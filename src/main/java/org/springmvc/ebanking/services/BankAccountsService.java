package org.springmvc.ebanking.services;

import org.springmvc.ebanking.entities.BankAccount;
import org.springmvc.ebanking.entities.CurrentAccount;
import org.springmvc.ebanking.entities.Customer;
import org.springmvc.ebanking.entities.SavingAccount;
import org.springmvc.ebanking.exceptions.BalanceNotSufficientException;
import org.springmvc.ebanking.exceptions.BankAccountNotFoundException;
import org.springmvc.ebanking.exceptions.CustomerNotFoundException;

import java.util.List;

public interface BankAccountsService {
   Customer saveCustomer (Customer customer);
   CurrentAccount saveCurrentBankAccount(double initialBalance, double overDraft , Long customerId) throws CustomerNotFoundException;
   SavingAccount saveSavingBankAccount(double initialBalance, double interestRate , Long customerId) throws CustomerNotFoundException;
   List<Customer> listCustomers();
   BankAccount getBankAccount(String accountId) throws BankAccountNotFoundException;
   void debit(String accountId, double amount ,String description ) throws BankAccountNotFoundException, BalanceNotSufficientException;
   void credit(String accountId, double amount ,String description ) throws BankAccountNotFoundException ;
   void transfer (String accountIdSource , String accountIdDestination , double amount ) throws BankAccountNotFoundException, BalanceNotSufficientException ;

}
