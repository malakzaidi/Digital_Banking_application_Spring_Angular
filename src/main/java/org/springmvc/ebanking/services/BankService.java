package org.springmvc.ebanking.services;

import org.springframework.stereotype.Service;
import org.springmvc.ebanking.entities.BankAccount;
import org.springmvc.ebanking.entities.CurrentAccount;
import org.springmvc.ebanking.entities.SavingAccount;
import org.springmvc.ebanking.repositories.BankAccountRepository;

@Service
public class BankService {
    private final BankAccountRepository bankAccountRepository;

    public BankService(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

    public void testAccountDetails(String accountId) {
        BankAccount bankAccount = bankAccountRepository.findById(accountId).orElse(null);
        if (bankAccount != null) {
            System.out.println("*****************************");
            System.out.println("Account ID: " + bankAccount.getId());
            System.out.println("Balance: " + bankAccount.getBalance());
            System.out.println("Status: " + bankAccount.getStatus());
            System.out.println("Created At: " + bankAccount.getCreatedAt());
            System.out.println("Customer: " + bankAccount.getCustomer().getName());
            System.out.println("Type: " + bankAccount.getClass().getSimpleName());
            if (bankAccount instanceof CurrentAccount) {
                System.out.println("Over Draft: " + ((CurrentAccount) bankAccount).getOverDraft());
            } else if (bankAccount instanceof SavingAccount) {
                System.out.println("Interest Rate: " + ((SavingAccount) bankAccount).getInterestRate());
            }
            bankAccount.getAccountOperations().forEach(op -> {
                System.out.println(op.getType() + "\t" + op.getOperationDate() + "\t" + op.getAmount());
            });
        } else {
            System.out.println("Account not found: " + accountId);
        }
    }
}