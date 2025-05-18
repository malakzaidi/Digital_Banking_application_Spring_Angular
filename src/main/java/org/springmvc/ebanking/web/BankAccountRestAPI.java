package org.springmvc.ebanking.web;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springmvc.ebanking.entities.BankAccount;
import org.springmvc.ebanking.exceptions.BankAccountNotFoundException;
import org.springmvc.ebanking.services.BankAccountsService;

import java.util.List;

@RestController
@CrossOrigin("*")
public class BankAccountRestAPI {
    private BankAccountsService bankAccountService;

    public BankAccountRestAPI(BankAccountsService bankAccountsService) {
        this.bankAccountService = bankAccountService;
    }

    @GetMapping("/accounts/{accountId}")
    public BankAccount getBankAccount(@PathVariable String accountId) throws BankAccountNotFoundException {
        return bankAccountService.getBankAccount(accountId);
    }
    @GetMapping("/accounts")
    public List<BankAccount> listAccounts(){
        return bankAccountService.bankAccountList();
    }
}

