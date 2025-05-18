package org.springmvc.ebanking.services;

import org.springmvc.ebanking.dtos.*;
import org.springmvc.ebanking.exceptions.BalanceNotSufficientException;
import org.springmvc.ebanking.exceptions.BankAccountNotFoundException;
import org.springmvc.ebanking.exceptions.CustomerNotFoundException;

import java.util.List;

public interface BankAccountsService {
   BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException;
   List<BankAccountDTO> bankAccountList();
   List<AccountOperationDTO> accountHistory(String accountId);
   AccountHistoryDTO getAccountHistory(String accountId, int page, int size) throws BankAccountNotFoundException;
   void debit(String accountId, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException;
   void credit(String accountId, double amount, String description) throws BankAccountNotFoundException;
   void transfer(String accountSource, String accountDestination, double amount) throws BankAccountNotFoundException, BalanceNotSufficientException;
   void deleteBankAccount(String accountId) throws BankAccountNotFoundException;
   CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException;

   CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId) throws CustomerNotFoundException;

   SavingBankAccountDTO saveSavingBankAccount(double initialBalance, double interestRate, Long customerId) throws CustomerNotFoundException;

   List<CustomerDTO> listCustomers();
   List<CustomerDTO> searchCustomers(String keyword);
   CustomerDTO saveCustomer(CustomerDTO customerDTO);
   CustomerDTO updateCustomer(CustomerDTO customerDTO);
   void deleteCustomer(Long customerId) throws CustomerNotFoundException ;
}