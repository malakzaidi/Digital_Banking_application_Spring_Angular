package org.springmvc.ebanking.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springmvc.ebanking.dtos.*;
import org.springmvc.ebanking.exceptions.BalanceNotSufficientException;
import org.springmvc.ebanking.exceptions.BankAccountNotFoundException;
import org.springmvc.ebanking.services.BankAccountsService;

import java.util.List;

@RestController
@Slf4j
@CrossOrigin("*")
public class BankAccountRestAPI {
    private BankAccountsService bankAccountService;

    public BankAccountRestAPI(BankAccountsService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @GetMapping("/accounts/{accountId}")
    public BankAccountDTO getBankAccount(@PathVariable String accountId) throws BankAccountNotFoundException {
        log.info("Fetching bank account: {}", accountId);
        return bankAccountService.getBankAccount(accountId);
    }

    @GetMapping("/accounts")
    public List<BankAccountDTO> listAccounts() {
        log.info("Fetching all bank accounts");
        return bankAccountService.bankAccountList();
    }

    @GetMapping("/accounts/{accountId}/operations")
    public List<AccountOperationDTO> getHistory(@PathVariable String accountId) {
        log.info("Fetching history for account: {}", accountId);
        return bankAccountService.accountHistory(accountId);
    }

    @GetMapping("/accounts/{accountId}/pageOperations")
    public AccountHistoryDTO getAccountHistory(
            @PathVariable String accountId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size) throws BankAccountNotFoundException {
        log.info("Fetching paged history for account: {}, page: {}, size: {}", accountId, page, size);
        return bankAccountService.getAccountHistory(accountId, page, size);
    }

    @PostMapping("/accounts/debit")
    public DebitDTO debit(@RequestBody DebitDTO debitDTO) throws BankAccountNotFoundException, BalanceNotSufficientException {
        log.info("Debiting account: {}", debitDTO);
        this.bankAccountService.debit(debitDTO.getAccountId(), debitDTO.getAmount(), debitDTO.getDescription());
        return debitDTO;
    }

    @PostMapping("/accounts/credit")
    public CreditDTO credit(@RequestBody CreditDTO creditDTO) throws BankAccountNotFoundException {
        log.info("Crediting account: {}", creditDTO);
        this.bankAccountService.credit(creditDTO.getAccountId(), creditDTO.getAmount(), creditDTO.getDescription());
        return creditDTO;
    }

    @PostMapping("/accounts/transfer")
    public void transfer(@RequestBody TransferRequestDTO transferRequestDTO) throws BankAccountNotFoundException, BalanceNotSufficientException {
        log.info("Transferring: {}", transferRequestDTO);
        this.bankAccountService.transfer(
                transferRequestDTO.getAccountSource(),
                transferRequestDTO.getAccountDestination(),
                transferRequestDTO.getAmount());
    }

    @DeleteMapping("/accounts/{accountId}")
    public void deleteBankAccount(@PathVariable String accountId) throws BankAccountNotFoundException {
        log.info("Deleting bank account: {}", accountId);
        bankAccountService.deleteBankAccount(accountId);
    }
}