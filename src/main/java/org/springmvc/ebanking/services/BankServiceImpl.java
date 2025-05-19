package org.springmvc.ebanking.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
import org.springmvc.ebanking.repositories.UserRepository;

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
    private UserRepository userRepository;
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
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    log.error("Customer not found: {}", customerId);
                    return new CustomerNotFoundException("Customer not found");
                });
        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setId(UUID.randomUUID().toString());
        currentAccount.setCreatedAt(new Date());
        currentAccount.setBalance(initialBalance);
        currentAccount.setOverDraft(overDraft);
        currentAccount.setCustomer(customer);

        // Set createdBy and updatedBy
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            String username = ((UserDetails) auth.getPrincipal()).getUsername();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            currentAccount.setCreatedBy(user);
            currentAccount.setUpdatedBy(user);
            log.info("Set createdBy and updatedBy to user: {} for current account: {}", user.getUsername(), currentAccount.getId());
        }

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
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    log.error("Customer not found: {}", customerId);
                    return new CustomerNotFoundException("Customer not found");
                });
        SavingAccount savingAccount = new SavingAccount();
        savingAccount.setId(UUID.randomUUID().toString());
        savingAccount.setCreatedAt(new Date());
        savingAccount.setBalance(initialBalance);
        savingAccount.setInterestRate(interestRate);
        savingAccount.setCustomer(customer);

        // Set createdBy and updatedBy
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            String username = ((UserDetails) auth.getPrincipal()).getUsername();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            savingAccount.setCreatedBy(user);
            savingAccount.setUpdatedBy(user);
            log.info("Set createdBy and updatedBy to user: {} for saving account: {}", user.getUsername(), savingAccount.getId());
        }

        SavingAccount savedBankAccount = bankAccountRepository.save(savingAccount);
        log.info("Saving account saved successfully: {}", savedBankAccount.getId());
        return dtoMapper.fromSavingBankAccount(savedBankAccount);
    }

    @Override
    public void deleteBankAccount(String accountId) throws BankAccountNotFoundException {
        log.info("Service: Deleting bank account: {}", accountId);
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.error("Bank account not found: {}", accountId);
                    return new BankAccountNotFoundException("Bank account not found: " + accountId);
                });
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
                .orElseThrow(() -> {
                    log.error("Customer not found: {}", customerId);
                    return new CustomerNotFoundException("Customer not found: " + customerId);
                });
        List<BankAccount> accounts = bankAccountRepository.findByCustomerId(customerId);
        if (!accounts.isEmpty()) {
            log.info("Deleting {} bank accounts for customer ID: {}", accounts.size(), customerId);
            List<String> nonZeroBalanceAccounts = new java.util.ArrayList<>();
            for (BankAccount account : accounts) {
                try {
                    deleteBankAccount(account.getId());
                } catch (BankAccountNotFoundException e) {
                    log.error("Bank account {} not found for customer {}: {}", account.getId(), customerId, e.getMessage(), e);
                } catch (IllegalStateException e) {
                    log.warn("Failed to delete account {}: {}", account.getId(), e.getMessage(), e);
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
        log.info("Saving new Customer: {}", customerDTO.getName());
        Customer customer = dtoMapper.fromCustomerDTO(customerDTO);

        // Set createdBy and updatedBy
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            String username = ((UserDetails) auth.getPrincipal()).getUsername();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            customer.setCreatedBy(user);
            customer.setUpdatedBy(user);
            log.info("Set createdBy and updatedBy to user: {} for customer: {}", user.getUsername(), customer.getName());
        }

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
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.error("Bank account not found: {}", accountId);
                    return new BankAccountNotFoundException("Bank account not found: " + accountId);
                });
        if (bankAccount instanceof SavingAccount) {
            return dtoMapper.fromSavingBankAccount((SavingAccount) bankAccount);
        } else {
            return dtoMapper.fromCurrentBankAccount((CurrentAccount) bankAccount);
        }
    }

    @Override
    public void debit(String accountId, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException {
        log.info("Debit operation on account: {}, amount: {}, description: {}", accountId, amount, description);
        if (amount <= 0) {
            log.warn("Invalid debit amount: {} for account: {}", amount, accountId);
            throw new IllegalArgumentException("Debit amount must be positive");
        }
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.error("Bank account not found: {}", accountId);
                    return new BankAccountNotFoundException("Bank account not found: " + accountId);
                });
        if (bankAccount.getBalance() < amount) {
            log.warn("Insufficient balance for debit: {} < {} for account: {}", bankAccount.getBalance(), amount, accountId);
            throw new BalanceNotSufficientException("Balance not sufficient");
        }
        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setType(OperationType.DEBIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setOperationDate(new Date());
        accountOperation.setBankAccount(bankAccount);

        // Set performedBy and update updatedBy on bank account
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            String username = ((UserDetails) auth.getPrincipal()).getUsername();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            accountOperation.setPerformedBy(user);
            bankAccount.setUpdatedBy(user);
            log.info("Set performedBy to user: {} for debit operation on account: {}", user.getUsername(), accountId);
        }

        accountOperationRepository.save(accountOperation);
        bankAccount.setBalance(bankAccount.getBalance() - amount);
        bankAccountRepository.save(bankAccount);
        log.info("Debit operation completed on account: {}, new balance: {}", accountId, bankAccount.getBalance());
    }

    @Override
    public void credit(String accountId, double amount, String description) throws BankAccountNotFoundException {
        log.info("Credit operation on account: {}, amount: {}, description: {}", accountId, amount, description);
        if (amount <= 0) {
            log.warn("Invalid credit amount: {} for account: {}", amount, accountId);
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.error("Bank account not found: {}", accountId);
                    return new BankAccountNotFoundException("Bank account not found: " + accountId);
                });
        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setType(OperationType.CREDIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setOperationDate(new Date());
        accountOperation.setBankAccount(bankAccount);

        // Set performedBy and update updatedBy on bank account
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            String username = ((UserDetails) auth.getPrincipal()).getUsername();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            accountOperation.setPerformedBy(user);
            bankAccount.setUpdatedBy(user);
            log.info("Set performedBy to user: {} for credit operation on account: {}", user.getUsername(), accountId);
        }

        accountOperationRepository.save(accountOperation);
        bankAccount.setBalance(bankAccount.getBalance() + amount);
        bankAccountRepository.save(bankAccount);
        log.info("Credit operation completed on account: {}, new balance: {}", accountId, bankAccount.getBalance());
    }

    @Override
    public void transfer(String accountIdSource, String accountIdDestination, double amount) throws BankAccountNotFoundException, BalanceNotSufficientException {
        log.info("Transferring {} from account {} to account {}", amount, accountIdSource, accountIdDestination);
        debit(accountIdSource, amount, "Transfer to " + accountIdDestination);
        credit(accountIdDestination, amount, "Transfer from " + accountIdSource);
        log.info("Transfer completed successfully");
    }

    @Override
    public List<BankAccountDTO> bankAccountList() {
        List<BankAccount> bankAccounts = bankAccountRepository.findAll();
        return bankAccounts.stream().map(bankAccount -> {
            if (bankAccount instanceof CurrentAccount) {
                return dtoMapper.fromCurrentBankAccount((CurrentAccount) bankAccount);
            } else if (bankAccount instanceof SavingAccount) {
                return dtoMapper.fromSavingBankAccount((SavingAccount) bankAccount);
            } else {
                log.warn("Unexpected account type: {}", bankAccount.getClass().getName());
                return dtoMapper.fromBankAccount(bankAccount);
            }
        }).collect(Collectors.toList());
    }

    @Override
    public CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    log.error("Customer not found: {}", customerId);
                    return new CustomerNotFoundException("Customer not found: " + customerId);
                });
        return dtoMapper.fromCustomer(customer);
    }

    @Override
    public CustomerDTO updateCustomer(CustomerDTO customerDTO) {
        log.info("Updating customer: {}", customerDTO.getName());
        Customer customer = dtoMapper.fromCustomerDTO(customerDTO);

        // Set updatedBy
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            String username = ((UserDetails) auth.getPrincipal()).getUsername();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            customer.setUpdatedBy(user);
            log.info("Set updatedBy to user: {} for customer: {}", user.getUsername(), customer.getName());
        }

        Customer savedCustomer = customerRepository.save(customer);
        return dtoMapper.fromCustomer(savedCustomer);
    }

    @Override
    public List<AccountOperationDTO> accountHistory(String accountId) {
        List<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountId(accountId);
        return accountOperations.stream()
                .map(op -> dtoMapper.fromAccountOperation(op))
                .collect(Collectors.toList());
    }

    @Override
    public AccountHistoryDTO getAccountHistory(String accountId, int page, int size) throws BankAccountNotFoundException {
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.error("Bank account not found: {}", accountId);
                    return new BankAccountNotFoundException("Account not found: " + accountId);
                });
        Page<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountIdOrderByOperationDateDesc(accountId, PageRequest.of(page, size));
        AccountHistoryDTO accountHistoryDTO = new AccountHistoryDTO();
        List<AccountOperationDTO> accountOperationDTOS = accountOperations.getContent().stream()
                .map(op -> dtoMapper.fromAccountOperation(op))
                .collect(Collectors.toList());
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
        List<Customer> customers = customerRepository.searchCustomer(keyword);
        List<CustomerDTO> customerDTOS = customers.stream()
                .map(cust -> dtoMapper.fromCustomer(cust))
                .collect(Collectors.toList());
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