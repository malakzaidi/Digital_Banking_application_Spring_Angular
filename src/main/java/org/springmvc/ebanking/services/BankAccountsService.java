package org.springmvc.ebanking.services;

import org.springmvc.ebanking.dtos.*;
import org.springmvc.ebanking.entities.Customer;
import org.springmvc.ebanking.exceptions.BalanceNotSufficientException;
import org.springmvc.ebanking.exceptions.BankAccountNotFoundException;
import org.springmvc.ebanking.exceptions.CustomerNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface BankAccountsService {

   CustomerDTO saveCustomer(CustomerDTO customerDTO) throws CustomerNotFoundException;

   CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId, String userId) throws CustomerNotFoundException;

   SavingBankAccountDTO saveSavingBankAccount(double initialBalance, double interestRate, Long customerId, String userId) throws CustomerNotFoundException;

   // New method for user account creation
   BankAccountDTO createUserAccount(double initialBalance, String userId) throws CustomerNotFoundException;

   List<CustomerDTO> listCustomers();

   BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException;

   void debit(String accountId, double amount, String description, String userId) throws BankAccountNotFoundException, BalanceNotSufficientException;

   void credit(String accountId, double amount, String description, String userId) throws BankAccountNotFoundException;

   void transfer(String accountIdSource, String accountIdDestination, double amount, String userId) throws BankAccountNotFoundException, BalanceNotSufficientException;

   // New methods for user transactions
   void userDebit(String userId, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException, CustomerNotFoundException;

   void userCredit(String userId, double amount, String description) throws BankAccountNotFoundException, CustomerNotFoundException;

   // New method for user transfers
   void userTransfer(String userId, String recipientIdentifier, double amount) throws BankAccountNotFoundException, BalanceNotSufficientException, CustomerNotFoundException;

   List<BankAccountDTO> bankAccountList();

   Page<BankAccountDTO> bankAccountList(Pageable pageable);

   CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException;

   Optional<Customer> findCustomerById(Long customerId);

   CustomerDTO updateCustomer(CustomerDTO customerDTO) throws CustomerNotFoundException;

   void deleteCustomer(Long customerId);

   List<CustomerDTO> searchCustomers(String keyword);

   List<AccountOperationDTO> accountHistory(String accountId);

   AccountHistoryDTO getAccountHistory(String accountId, int page, int size) throws BankAccountNotFoundException;

   Page<BankAccountDTO> searchBankAccounts(String keyword, Pageable pageable);

   List<BankAccountDTO> getUserAccounts(String userId) throws CustomerNotFoundException;

   void deleteBankAccount(String accountId) throws BankAccountNotFoundException;

   void payBill(BillPaymentDTO billPaymentDTO) throws BankAccountNotFoundException, BalanceNotSufficientException;

   List<TransactionHistoryDTO> getTransactionHistory(String userId, Pageable pageable) throws CustomerNotFoundException;

   DashboardDTO getDashboardData(String userId, Pageable pageable) throws CustomerNotFoundException;


}