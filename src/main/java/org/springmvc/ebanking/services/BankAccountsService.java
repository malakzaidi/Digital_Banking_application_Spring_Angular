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
   CustomerDTO saveCustomer(CustomerDTO customerDTO);
   CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId) throws CustomerNotFoundException;
   SavingBankAccountDTO saveSavingBankAccount(double initialBalance, double interestRate, Long customerId) throws CustomerNotFoundException;
   List<CustomerDTO> listCustomers();
   Page<BankAccountDTO> bankAccountList(Pageable pageable);
   BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException;
   void debit(String accountId, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException;
   void credit(String accountId, double amount, String description) throws BankAccountNotFoundException;
   void transfer(String accountIdSource, String accountIdDestination, double amount) throws BankAccountNotFoundException, BalanceNotSufficientException;
   CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException;
   CustomerDTO updateCustomer(CustomerDTO customerDTO);
   void deleteCustomer(Long customerId) throws CustomerNotFoundException;
   void deleteBankAccount(String accountId) throws BankAccountNotFoundException;
   List<AccountOperationDTO> accountHistory(String accountId);
   AccountHistoryDTO getAccountHistory(String accountId, int page, int size) throws BankAccountNotFoundException;
   List<CustomerDTO> searchCustomers(String keyword);
   Page<BankAccountDTO> searchBankAccounts(String keyword, Pageable pageable); // Add pagination
   List<BankAccountDTO> getUserAccounts() throws CustomerNotFoundException; // Add user-specific accounts
   Optional<Customer> findCustomerById(Long id);
}