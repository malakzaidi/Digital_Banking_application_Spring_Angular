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


    }
