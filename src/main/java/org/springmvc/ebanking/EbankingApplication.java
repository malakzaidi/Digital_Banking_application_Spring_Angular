package org.springmvc.ebanking;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springmvc.ebanking.entities.*;
import org.springmvc.ebanking.enums.AccountStatus;
import org.springmvc.ebanking.enums.OperationType;
import org.springmvc.ebanking.exceptions.BalanceNotSufficientException;
import org.springmvc.ebanking.exceptions.BankAccountNotFoundException;
import org.springmvc.ebanking.exceptions.CustomerNotFoundException;
import org.springmvc.ebanking.repositories.AccountOperationRepository;
import org.springmvc.ebanking.repositories.BankAccountRepository;
import org.springmvc.ebanking.repositories.CustomerRepository;
import org.springmvc.ebanking.services.BankAccountsService;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootApplication
public class EbankingApplication {

    public static void main(String[] args) {
        SpringApplication.run(EbankingApplication.class, args);
    }
    @Bean
    CommandLineRunner commandLineRunner(BankAccountsService bankAccountsService){
        return args -> {
            Stream.of("Say","Siham","Samra").forEach(name->{
                Customer customer=new Customer();
                customer.setName(name);
                customer.setEmail(name+"@gmail.com");
                bankAccountsService.saveCustomer(customer);
            });
            bankAccountsService.listCustomers().forEach(customer->{
                try {
                    bankAccountsService.saveCurrentBankAccount(Math.random()*90000,9000,customer.getId());
                    bankAccountsService.saveSavingBankAccount(Math.random()*120000,5.5,customer.getId());
                    List<BankAccount> bankAccounts = bankAccountsService.bankAccountList();
                    for (BankAccount bankAccount:bankAccounts){
                        for (int i = 0; i <10 ; i++) {
                            bankAccountsService.credit(bankAccount.getId(), 10000+Math.random()*120000,"Credit");
                            bankAccountsService.debit(bankAccount.getId(), 1000+Math.random()*9000,"Debit");
                        }
                    }
                } catch (CustomerNotFoundException e) {
                    e.printStackTrace();
                } catch (BankAccountNotFoundException e) {
                    e.printStackTrace();
                } catch (BalanceNotSufficientException e) {
                    e.printStackTrace();
                }
            });

        };
    }
    //@Bean
    CommandLineRunner start(CustomerRepository customerRepository,
                            BankAccountRepository bankAccountRepository,
                            AccountOperationRepository accountOperationRepository){
        return args -> {
            Stream.of("Touria","Nada","Malak").forEach(name->{
                Customer customer=new Customer();
                customer.setName(name);
                customer.setEmail(name+"@gmail.com");
                customerRepository.save(customer);
            });
            customerRepository.findAll().forEach(cust->{
                CurrentAccount currentAccount=new CurrentAccount();
                currentAccount.setId(UUID.randomUUID().toString());
                currentAccount.setBalance(Math.random()*90000);
                currentAccount.setCreatedAt(new Date());
                currentAccount.setStatus(AccountStatus.CREATED);
                currentAccount.setCustomer(cust);
                currentAccount.setOverDraft(9000);
                bankAccountRepository.save(currentAccount);

                SavingAccount savingAccount=new SavingAccount();
                savingAccount.setId(UUID.randomUUID().toString());
                savingAccount.setBalance(Math.random()*90000);
                savingAccount.setCreatedAt(new Date());
                savingAccount.setStatus(AccountStatus.CREATED);
                savingAccount.setCustomer(cust);
                savingAccount.setInterestRate(5.5);
                bankAccountRepository.save(savingAccount);

            });
            bankAccountRepository.findAll().forEach(acc->{
                for (int i = 0; i <10 ; i++) {
                    AccountOperation accountOperation=new AccountOperation();
                    accountOperation.setOperationDate(new Date());
                    accountOperation.setAmount(Math.random()*12000);
                    accountOperation.setType(Math.random()>0.5? OperationType.DEBIT: OperationType.CREDIT);
                    accountOperation.setBankAccount(acc);
                    accountOperationRepository.save(accountOperation);
                }

            });
        };

    }

}