package org.springmvc.ebanking.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springmvc.ebanking.dtos.*;
import org.springmvc.ebanking.entities.*;
import org.springmvc.ebanking.enums.AccountStatus;
import org.springmvc.ebanking.enums.OperationType;
import org.springmvc.ebanking.exceptions.BalanceNotSufficientException;
import org.springmvc.ebanking.exceptions.BankAccountNotFoundException;
import org.springmvc.ebanking.exceptions.CustomerNotFoundException;
import org.springmvc.ebanking.mappers.BankAccountMapperImpl;
import org.springmvc.ebanking.repositories.AccountOperationRepository;
import org.springmvc.ebanking.repositories.BankAccountRepository;
import org.springmvc.ebanking.repositories.CustomerRepository;
import org.springmvc.ebanking.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public CustomerDTO saveCustomer(CustomerDTO customerDTO) throws CustomerNotFoundException {
        log.info("Saving new Customer {}", customerDTO);
        Customer customer = new Customer();
        customer.setId(customerDTO.getId());
        customer.setName(customerDTO.getName());
        customer.setEmail(customerDTO.getEmail());
        User user = userRepository.findByUsername(customerDTO.getCreatedBy())
                .orElseThrow(() -> new CustomerNotFoundException("User not found for creation: " + customerDTO.getCreatedBy()));
        customer.setCreatedBy(user);
        customer.setUpdatedBy(user);
        Customer savedCustomer = customerRepository.save(customer);
        return dtoMapper.fromCustomer(savedCustomer);
    }

    @Override
    public CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId, String userId) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        User user = userRepository.findByUsername(userId)
                .orElseThrow(() -> new CustomerNotFoundException("User not found: " + userId));
        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setId(UUID.randomUUID().toString());
        currentAccount.setCreatedAt(new Date());
        currentAccount.setBalance(initialBalance);
        currentAccount.setStatus(AccountStatus.CREATED);
        currentAccount.setCustomer(customer);
        currentAccount.setCreatedBy(user);
        currentAccount.setUpdatedBy(user);
        currentAccount.setOverDraft(overDraft);
        CurrentAccount savedAccount = bankAccountRepository.save(currentAccount);
        return dtoMapper.fromCurrentBankAccount(savedAccount);
    }

    @Override
    public SavingBankAccountDTO saveSavingBankAccount(double initialBalance, double interestRate, Long customerId, String userId) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        User user = userRepository.findByUsername(userId)
                .orElseThrow(() -> new CustomerNotFoundException("User not found: " + userId));
        SavingAccount savingAccount = new SavingAccount();
        savingAccount.setId(UUID.randomUUID().toString());
        savingAccount.setCreatedAt(new Date());
        savingAccount.setBalance(initialBalance);
        savingAccount.setStatus(AccountStatus.CREATED);
        savingAccount.setCustomer(customer);
        savingAccount.setCreatedBy(user);
        savingAccount.setUpdatedBy(user);
        savingAccount.setInterestRate(interestRate);
        SavingAccount savedAccount = bankAccountRepository.save(savingAccount);
        return dtoMapper.fromSavingBankAccount(savedAccount);
    }

    @Override
    public BankAccountDTO createUserAccount(double initialBalance, String userId) throws CustomerNotFoundException {
        User user = userRepository.findByUsername(userId)
                .orElseThrow(() -> new CustomerNotFoundException("User not found: " + userId));
        Customer customer = customerRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found for user: " + userId));
        CurrentAccount basicAccount = new CurrentAccount();
        basicAccount.setId(UUID.randomUUID().toString());
        basicAccount.setCreatedAt(new Date());
        basicAccount.setBalance(initialBalance);
        basicAccount.setStatus(AccountStatus.CREATED);
        basicAccount.setCustomer(customer);
        basicAccount.setCreatedBy(user);
        basicAccount.setUpdatedBy(user);
        basicAccount.setOverDraft(500); // Default overdraft for basic account
        CurrentAccount savedAccount = bankAccountRepository.save(basicAccount);
        return dtoMapper.fromCurrentBankAccount(savedAccount);
    }

    @Override
    public List<CustomerDTO> listCustomers() {
        return customerRepository.findAll().stream()
                .map(dtoMapper::fromCustomer)
                .collect(Collectors.toList());
    }

    @Override
    public BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException {
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("BankAccount not found"));
        if (bankAccount instanceof SavingAccount) {
            return dtoMapper.fromSavingBankAccount((SavingAccount) bankAccount);
        } else {
            return dtoMapper.fromCurrentBankAccount((CurrentAccount) bankAccount);
        }
    }

    @Override
    public void debit(String accountId, double amount, String description, String userId) throws BankAccountNotFoundException, BalanceNotSufficientException {
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("BankAccount not found"));
        User user = userRepository.findByUsername(userId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));
        if (bankAccount.getBalance() < amount) {
            throw new BalanceNotSufficientException("Balance not sufficient");
        }
        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setType(OperationType.DEBIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setOperationDate(new Date());
        accountOperation.setBankAccount(bankAccount);
        accountOperation.setPerformedBy(user);
        accountOperationRepository.save(accountOperation);
        bankAccount.setBalance(bankAccount.getBalance() - amount);
        bankAccount.setUpdatedBy(user);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void credit(String accountId, double amount, String description, String userId) throws BankAccountNotFoundException {
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("BankAccount not found"));
        User user = userRepository.findByUsername(userId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));
        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setType(OperationType.CREDIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setOperationDate(new Date());
        accountOperation.setBankAccount(bankAccount);
        accountOperation.setPerformedBy(user);
        accountOperationRepository.save(accountOperation);
        bankAccount.setBalance(bankAccount.getBalance() + amount);
        bankAccount.setUpdatedBy(user);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void transfer(String accountIdSource, String accountIdDestination, double amount, String userId) throws BankAccountNotFoundException, BalanceNotSufficientException {
        debit(accountIdSource, amount, "Transfer to " + accountIdDestination, userId);
        credit(accountIdDestination, amount, "Transfer from " + accountIdSource, userId);
    }

    @Override
    public void userDebit(String userId, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException, CustomerNotFoundException {
        String accountId = getPrimaryAccountId(userId);
        debit(accountId, amount, description, userId);
    }

    @Override
    public void userCredit(String userId, double amount, String description) throws BankAccountNotFoundException, CustomerNotFoundException {
        String accountId = getPrimaryAccountId(userId);
        credit(accountId, amount, description, userId);
    }

    @Override
    public void userTransfer(String userId, String recipientIdentifier, double amount) throws BankAccountNotFoundException, BalanceNotSufficientException, CustomerNotFoundException {
        String sourceAccountId = getPrimaryAccountId(userId);
        String destinationAccountId = getAccountIdByIdentifier(recipientIdentifier);
        transfer(sourceAccountId, destinationAccountId, amount, userId);
    }

    private String getPrimaryAccountId(String userId) throws CustomerNotFoundException, BankAccountNotFoundException {
        User user = userRepository.findByUsername(userId)
                .orElseThrow(() -> new CustomerNotFoundException("User not found: " + userId));
        Customer customer = customerRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found for user: " + userId));
        List<BankAccount> accounts = bankAccountRepository.findByCustomerId(customer.getId());
        if (accounts.isEmpty()) {
            throw new BankAccountNotFoundException("No accounts found for user: " + userId);
        }
        return accounts.get(0).getId(); // Use the first account as the primary account
    }

    private String getAccountIdByIdentifier(String identifier) throws CustomerNotFoundException, BankAccountNotFoundException {
        User recipientUser = userRepository.findByUsername(identifier)
                .orElseThrow(() -> new CustomerNotFoundException("Recipient not found: " + identifier));
        Customer recipient = customerRepository.findByEmail(recipientUser.getEmail())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found for recipient: " + identifier));
        List<BankAccount> accounts = bankAccountRepository.findByCustomerId(recipient.getId());
        if (accounts.isEmpty()) {
            throw new BankAccountNotFoundException("No accounts found for recipient: " + identifier);
        }
        return accounts.get(0).getId(); // Use the first account as the recipient's account
    }

    @Override
    public List<BankAccountDTO> bankAccountList() {
        return bankAccountRepository.findAll().stream()
                .map(bankAccount -> {
                    if (bankAccount instanceof SavingAccount) {
                        return dtoMapper.fromSavingBankAccount((SavingAccount) bankAccount);
                    } else {
                        return dtoMapper.fromCurrentBankAccount((CurrentAccount) bankAccount);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<BankAccountDTO> bankAccountList(Pageable pageable) {
        return bankAccountRepository.findAll(pageable).map(bankAccount -> {
            if (bankAccount instanceof SavingAccount) {
                return dtoMapper.fromSavingBankAccount((SavingAccount) bankAccount);
            } else {
                return dtoMapper.fromCurrentBankAccount((CurrentAccount) bankAccount);
            }
        });
    }

    @Override
    public CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer Not found"));
        return dtoMapper.fromCustomer(customer);
    }

    @Override
    public Optional<Customer> findCustomerById(Long customerId) {
        return customerRepository.findById(customerId);
    }

    @Override
    public CustomerDTO updateCustomer(CustomerDTO customerDTO) throws CustomerNotFoundException {
        log.info("Updating Customer {}", customerDTO);
        Customer customer = customerRepository.findById(customerDTO.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        customer.setName(customerDTO.getName());
        customer.setEmail(customerDTO.getEmail());
        User user = userRepository.findByUsername(customerDTO.getUpdatedBy())
                .orElseThrow(() -> new CustomerNotFoundException("User not found for update: " + customerDTO.getUpdatedBy()));
        customer.setUpdatedBy(user);
        Customer updatedCustomer = customerRepository.save(customer);
        return dtoMapper.fromCustomer(updatedCustomer);
    }

    @Override
    public void deleteCustomer(Long customerId) {
        customerRepository.deleteById(customerId);
    }

    @Override
    public List<CustomerDTO> searchCustomers(String keyword) {
        List<Customer> customers = customerRepository.findByNameContains(keyword);
        return customers.stream()
                .map(dtoMapper::fromCustomer)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountOperationDTO> accountHistory(String accountId) {
        List<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountId(accountId);
        return accountOperations.stream()
                .map(dtoMapper::fromAccountOperation)
                .collect(Collectors.toList());
    }

    @Override
    public AccountHistoryDTO getAccountHistory(String accountId, int page, int size) throws BankAccountNotFoundException {
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("BankAccount not found"));
        Page<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountId(accountId, PageRequest.of(page, size));
        AccountHistoryDTO accountHistoryDTO = new AccountHistoryDTO();
        List<AccountOperationDTO> accountOperationDTOList = accountOperations.getContent().stream()
                .map(dtoMapper::fromAccountOperation)
                .collect(Collectors.toList());
        accountHistoryDTO.setAccountOperationDTOS(accountOperationDTOList);
        accountHistoryDTO.setAccountId(bankAccount.getId());
        accountHistoryDTO.setBalance(bankAccount.getBalance());
        accountHistoryDTO.setCurrentPage(page);
        accountHistoryDTO.setPageSize(size);
        accountHistoryDTO.setTotalPages(accountOperations.getTotalPages());
        return accountHistoryDTO;
    }

    @Override
    public Page<BankAccountDTO> searchBankAccounts(String keyword, Pageable pageable) {
        Page<BankAccount> bankAccounts = bankAccountRepository.findByCustomerNameContains(keyword, pageable);
        return bankAccounts.map(bankAccount -> {
            if (bankAccount instanceof SavingAccount) {
                return dtoMapper.fromSavingBankAccount((SavingAccount) bankAccount);
            } else {
                return dtoMapper.fromCurrentBankAccount((CurrentAccount) bankAccount);
            }
        });
    }

    @Override
    public List<BankAccountDTO> getUserAccounts(String userId) throws CustomerNotFoundException {
        User user = userRepository.findByUsername(userId)
                .orElseThrow(() -> new CustomerNotFoundException("User not found: " + userId));
        Customer customer = customerRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found for user: " + userId));
        return bankAccountRepository.findByCustomerId(customer.getId()).stream()
                .map(bankAccount -> {
                    if (bankAccount instanceof SavingAccount) {
                        return dtoMapper.fromSavingBankAccount((SavingAccount) bankAccount);
                    } else {
                        return dtoMapper.fromCurrentBankAccount((CurrentAccount) bankAccount);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public void deleteBankAccount(String accountId) throws BankAccountNotFoundException {
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("BankAccount not found"));
        bankAccountRepository.delete(bankAccount);
    }

    @Override
    public void payBill(BillPaymentDTO billPaymentDTO) throws BankAccountNotFoundException, BalanceNotSufficientException {
        debit(billPaymentDTO.getAccountId(), billPaymentDTO.getAmount(), billPaymentDTO.getDescription(), billPaymentDTO.getUserId());
    }

    @Override
    public List<TransactionHistoryDTO> getTransactionHistory(String userId, Pageable pageable) throws CustomerNotFoundException {
        User user = userRepository.findByUsername(userId)
                .orElseThrow(() -> new CustomerNotFoundException("User not found: " + userId));
        Customer customer = customerRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found for user: " + userId));
        List<BankAccount> accounts = bankAccountRepository.findByCustomerId(customer.getId());
        List<String> accountIds = accounts.stream().map(BankAccount::getId).collect(Collectors.toList());
        Page<AccountOperation> operations = accountOperationRepository.findByBankAccountIdIn(accountIds, pageable);
        return operations.getContent().stream()
                .map(op -> {
                    TransactionHistoryDTO dto = new TransactionHistoryDTO();
                    dto.setId(op.getId());
                    dto.setAmount(op.getAmount());
                    dto.setType(op.getType().toString());
                    dto.setDescription(op.getDescription());
                    dto.setDate(op.getOperationDate());
                    dto.setAccountId(op.getBankAccount().getId());
                    return dto;
                })
                .collect(Collectors.toList());
    }
    @Override
    public DashboardDTO getDashboardData(Pageable pageable) {
        DashboardDTO dashboard = new DashboardDTO();

        // Summary data
        dashboard.setTotalCustomers(customerRepository.count());
        dashboard.setTotalAccounts(bankAccountRepository.count());
        dashboard.setTotalBalance(bankAccountRepository.findAll().stream()
                .mapToDouble(BankAccount::getBalance)
                .sum());

        // Paginated recent transactions
        Page<AccountOperation> transactionPage = accountOperationRepository.findAll(pageable);
        dashboard.setRecentTransactions(transactionPage.getContent().stream()
                .map(dtoMapper::fromAccountOperation)
                .collect(Collectors.toList()));
        dashboard.setCurrentPage(transactionPage.getNumber());
        dashboard.setPageSize(transactionPage.getSize());
        dashboard.setTotalTransactions(transactionPage.getTotalElements());
        dashboard.setTotalPages(transactionPage.getTotalPages());

        return dashboard;
    }
}