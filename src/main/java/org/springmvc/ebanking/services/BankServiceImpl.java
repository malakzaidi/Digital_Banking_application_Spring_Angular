package org.springmvc.ebanking.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springmvc.ebanking.dtos.*;
import org.springmvc.ebanking.entities.*;
import org.springmvc.ebanking.enums.OperationType;
import org.springmvc.ebanking.exceptions.BalanceNotSufficientException;
import org.springmvc.ebanking.exceptions.BankAccountNotFoundException;
import org.springmvc.ebanking.exceptions.CustomerNotFoundException;
import org.springmvc.ebanking.mappers.BankAccountMapperImpl;
import org.springmvc.ebanking.repositories.AccountOperationRepository;
import org.springmvc.ebanking.repositories.BankAccountRepository;
import org.springmvc.ebanking.repositories.CustomerRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class BankServiceImpl implements BankAccountsService {
    private CustomerRepository customerRepository;
    private BankAccountRepository bankAccountRepository;
    private AccountOperationRepository accountOperationRepository;
    private BankAccountMapperImpl dtoMapper;

    @Override
    public List<BankAccountDTO> searchBankAccounts(String keyword) {
        log.info("Searching bank accounts with keyword (account ID, customer ID, or customer name): {}", keyword);
        List<BankAccount> accounts;
        if (keyword == null || keyword.trim().isEmpty()) {
            accounts = bankAccountRepository.findAll();
        } else {
            accounts = bankAccountRepository.findByIdOrCustomerIdOrCustomerName(keyword);
        }
        List<BankAccountDTO> accountDTOs = accounts.stream().map(account -> {
            if (account instanceof SavingAccount) {
                return dtoMapper.fromSavingBankAccount((SavingAccount) account);
            } else {
                return dtoMapper.fromCurrentBankAccount((CurrentAccount) account);
            }
        }).collect(Collectors.toList());
        log.info("Found {} bank accounts", accountDTOs.size());
        return accountDTOs;
    }

    @Override
    public CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId) throws CustomerNotFoundException {
        log.info("Saving current account for customer ID: {}, initialBalance: {}, overDraft: {}", customerId, initialBalance, overDraft);
        if (initialBalance < 0) {
            log.warn("Invalid initial balance: {} for customer ID: {}", initialBalance, customerId);
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        if (overDraft < 0) {
            log.warn("Invalid overDraft: {} for customer ID: {}", overDraft, customerId);
            throw new IllegalArgumentException("Overdraft cannot be negative");
        }
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null) {
            log.error("Customer not found: {}", customerId);
            throw new CustomerNotFoundException("Customer not found");
        }
        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setId(UUID.randomUUID().toString());
        currentAccount.setCreatedAt(new Date());
        currentAccount.setBalance(initialBalance);
        currentAccount.setOverDraft(overDraft);
        currentAccount.setCustomer(customer);
        CurrentAccount savedBankAccount = bankAccountRepository.save(currentAccount);
        log.info("Current account saved successfully: {}", savedBankAccount.getId());
        return dtoMapper.fromCurrentBankAccount(savedBankAccount);
    }

    @Override
    public SavingBankAccountDTO saveSavingBankAccount(double initialBalance, double interestRate, Long customerId) throws CustomerNotFoundException {
        log.info("Saving saving account for customer ID: {}, initialBalance: {}, interestRate: {}", customerId, initialBalance, interestRate);
        if (initialBalance < 0) {
            log.warn("Invalid initial balance: {} for customer ID: {}", initialBalance, customerId);
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        if (interestRate < 0) {
            log.warn("Invalid interestRate: {} for customer ID: {}", interestRate, customerId);
            throw new IllegalArgumentException("Interest rate cannot be negative");
        }
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null) {
            log.error("Customer not found: {}", customerId);
            throw new CustomerNotFoundException("Customer not found");
        }
        SavingAccount savingAccount = new SavingAccount();
        savingAccount.setId(UUID.randomUUID().toString());
        savingAccount.setCreatedAt(new Date());
        savingAccount.setBalance(initialBalance);
        savingAccount.setInterestRate(interestRate);
        savingAccount.setCustomer(customer);
        SavingAccount savedBankAccount = bankAccountRepository.save(savingAccount);
        log.info("Saving account saved successfully: {}", savedBankAccount.getId());
        return dtoMapper.fromSavingBankAccount(savedBankAccount);
    }

    @Override
    public void deleteBankAccount(String accountId) throws BankAccountNotFoundException {
        log.info("Service: Deleting bank account: {}", accountId);
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("Bank account not found: " + accountId));
        if (account.getBalance() != 0) {
            log.warn("Cannot delete account {} with non-zero balance: {}", accountId, account.getBalance());
            throw new IllegalStateException("Cannot delete account with non-zero balance: " + accountId);
        }
        List<AccountOperation> operations = accountOperationRepository.findByBankAccountId(accountId);
        if (!operations.isEmpty()) {
            log.info("Deleting {} account operations for bank account: {}", operations.size(), accountId);
            accountOperationRepository.deleteAll(operations);
        }
        bankAccountRepository.delete(account);
        log.info("Bank account deleted successfully: {}", accountId);
    }

    @Override
    public void deleteCustomer(Long customerId) throws CustomerNotFoundException {
        log.info("Deleting customer with ID: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + customerId));
        List<BankAccount> accounts = bankAccountRepository.findByCustomerId(customerId);
        if (!accounts.isEmpty()) {
            log.info("Deleting {} bank accounts for customer ID: {}", accounts.size(), customerId);
            List<String> nonZeroBalanceAccounts = new java.util.ArrayList<>();
            for (BankAccount account : accounts) {
                try {
                    deleteBankAccount(account.getId());
                } catch (BankAccountNotFoundException e) {
                    log.error("Bank account {} not found for customer {}", account.getId(), customerId);
                } catch (IllegalStateException e) {
                    log.warn("Failed to delete account {}: {}", account.getId(), e.getMessage());
                    nonZeroBalanceAccounts.add(account.getId());
                }
            }
            if (!nonZeroBalanceAccounts.isEmpty()) {
                String message = "Cannot delete customer due to non-zero balance in accounts: " + String.join(", ", nonZeroBalanceAccounts);
                log.warn(message);
                throw new IllegalStateException(message);
            }
        }
        customerRepository.delete(customer);
        log.info("Customer deleted successfully: {}", customerId);
    }

    @Override
    public CustomerDTO saveCustomer(CustomerDTO customerDTO) {
        log.info("Saving new Customer");
        Customer customer=dtoMapper.fromCustomerDTO(customerDTO);
        Customer savedCustomer = customerRepository.save(customer);
        return dtoMapper.fromCustomer(savedCustomer);
    }

    @Override
    public List<CustomerDTO> listCustomers() {
        List<Customer> customers = customerRepository.findAll();
        List<CustomerDTO> customerDTOS = customers.stream()
                .map(customer -> dtoMapper.fromCustomer(customer))
                .collect(Collectors.toList());
        return customerDTOS;
    }

    @Override
    public BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException {
        BankAccount bankAccount=bankAccountRepository.findById(accountId)
                .orElseThrow(()->new BankAccountNotFoundException("BankAccount not found"));
        if(bankAccount instanceof SavingAccount){
            SavingAccount savingAccount= (SavingAccount) bankAccount;
            return dtoMapper.fromSavingBankAccount(savingAccount);
        } else {
            CurrentAccount currentAccount= (CurrentAccount) bankAccount;
            return dtoMapper.fromCurrentBankAccount(currentAccount);
        }
    }

    @Override
    public void debit(String accountId, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException {
        BankAccount bankAccount=bankAccountRepository.findById(accountId)
                .orElseThrow(()->new BankAccountNotFoundException("BankAccount not found"));
        if(bankAccount.getBalance()<amount)
            throw new BalanceNotSufficientException("Balance not sufficient");
        AccountOperation accountOperation=new AccountOperation();
        accountOperation.setType(OperationType.DEBIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setOperationDate(new Date());
        accountOperation.setBankAccount(bankAccount);
        accountOperationRepository.save(accountOperation);
        bankAccount.setBalance(bankAccount.getBalance()-amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void credit(String accountId, double amount, String description) throws BankAccountNotFoundException {
        BankAccount bankAccount=bankAccountRepository.findById(accountId)
                .orElseThrow(()->new BankAccountNotFoundException("BankAccount not found"));
        AccountOperation accountOperation=new AccountOperation();
        accountOperation.setType(OperationType.CREDIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setOperationDate(new Date());
        accountOperation.setBankAccount(bankAccount);
        accountOperationRepository.save(accountOperation);
        bankAccount.setBalance(bankAccount.getBalance()+amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void transfer(String accountIdSource, String accountIdDestination, double amount) throws BankAccountNotFoundException, BalanceNotSufficientException {
        debit(accountIdSource,amount,"Transfer to "+accountIdDestination);
        credit(accountIdDestination,amount,"Transfer from "+accountIdSource);
    }

    @Override
    public List<BankAccountDTO> bankAccountList() {
        List<BankAccount> bankAccounts = bankAccountRepository.findAll();
        return bankAccounts.stream().map(bankAccount -> {
            // Get customer information
            Long customerId = null;
            String customerName = null;
            if (bankAccount.getCustomer() != null) {
                customerId = bankAccount.getCustomer().getId();
                customerName = bankAccount.getCustomer().getName();
            }

            if (bankAccount instanceof CurrentAccount) {
                CurrentBankAccountDTO dto = new CurrentBankAccountDTO();
                dto.setId(bankAccount.getId());
                dto.setBalance(bankAccount.getBalance());
                dto.setCustomerId(customerId);
                dto.setCustomerName(customerName); // Set customer name
                dto.setType("CurrentAccount");
                dto.setCreatedAt(bankAccount.getCreatedAt());
                dto.setOverDraft(((CurrentAccount) bankAccount).getOverDraft());
                return dto;
            } else if (bankAccount instanceof SavingAccount) {
                SavingBankAccountDTO dto = new SavingBankAccountDTO();
                dto.setId(bankAccount.getId());
                dto.setBalance(bankAccount.getBalance());
                dto.setCustomerId(customerId);
                dto.setCustomerName(customerName); // Set customer name
                dto.setType("SavingAccount");
                dto.setCreatedAt(bankAccount.getCreatedAt());
                dto.setInterestRate(((SavingAccount) bankAccount).getInterestRate());
                return dto;
            }

            // Fallback for unknown types
            BankAccountDTO dto = new BankAccountDTO();
            dto.setId(bankAccount.getId());
            dto.setBalance(bankAccount.getBalance());
            dto.setCustomerId(customerId);
            dto.setCustomerName(customerName); // Set customer name
            dto.setType("Unknown");
            dto.setCreatedAt(bankAccount.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer Not found"));
        return dtoMapper.fromCustomer(customer);
    }

    @Override
    public CustomerDTO updateCustomer(CustomerDTO customerDTO) {
        log.info("Saving new Customer");
        Customer customer=dtoMapper.fromCustomerDTO(customerDTO);
        Customer savedCustomer = customerRepository.save(customer);
        return dtoMapper.fromCustomer(savedCustomer);
    }

    @Override
    public List<AccountOperationDTO> accountHistory(String accountId){
        List<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountId(accountId);
        return accountOperations.stream().map(op->dtoMapper.fromAccountOperation(op)).collect(Collectors.toList());
    }

    @Override
    public AccountHistoryDTO getAccountHistory(String accountId, int page, int size) throws BankAccountNotFoundException {
        BankAccount bankAccount=bankAccountRepository.findById(accountId).orElse(null);
        if(bankAccount==null) throw new BankAccountNotFoundException("Account not Found");
        Page<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountIdOrderByOperationDateDesc(accountId, PageRequest.of(page, size));
        AccountHistoryDTO accountHistoryDTO=new AccountHistoryDTO();
        List<AccountOperationDTO> accountOperationDTOS = accountOperations.getContent().stream().map(op -> dtoMapper.fromAccountOperation(op)).collect(Collectors.toList());
        accountHistoryDTO.setAccountOperationDTOS(accountOperationDTOS);
        accountHistoryDTO.setAccountId(bankAccount.getId());
        accountHistoryDTO.setBalance(bankAccount.getBalance());
        accountHistoryDTO.setCurrentPage(page);
        accountHistoryDTO.setPageSize(size);
        accountHistoryDTO.setTotalPages(accountOperations.getTotalPages());
        return accountHistoryDTO;
    }

    @Override
    public List<CustomerDTO> searchCustomers(String keyword) {
        List<Customer> customers=customerRepository.searchCustomer(keyword);
        List<CustomerDTO> customerDTOS = customers.stream().map(cust -> dtoMapper.fromCustomer(cust)).collect(Collectors.toList());
        return customerDTOS;
    }
    @Override
    public Optional<Customer> findCustomerById(Long id) {
        if (id == null) {
            log.warn("Attempted to find customer with null ID");
            return Optional.empty();
        }
        log.info("Fetching customer with ID: {}", id);
        return customerRepository.findById(id);
    }
}