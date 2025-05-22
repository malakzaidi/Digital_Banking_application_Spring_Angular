package org.springmvc.ebanking.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springmvc.ebanking.dtos.*;
import org.springmvc.ebanking.exceptions.BalanceNotSufficientException;
import org.springmvc.ebanking.exceptions.BankAccountNotFoundException;
import org.springmvc.ebanking.exceptions.CustomerNotFoundException;
import org.springmvc.ebanking.services.BankAccountsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api")
public class BankAccountRestAPI {

    private final BankAccountsService bankAccountService;

    public BankAccountRestAPI(BankAccountsService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @GetMapping("/accounts/{accountId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public BankAccountDTO getBankAccount(@PathVariable String accountId) throws BankAccountNotFoundException {
        log.info("Fetching bank account: {}", accountId);
        return bankAccountService.getBankAccount(accountId);
    }

    @GetMapping("/accounts")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> listAccounts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Fetching all bank accounts, page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<BankAccountDTO> accountPage = bankAccountService.bankAccountList(pageable);
        Map<String, Object> response = new HashMap<>();
        response.put("accounts", accountPage.getContent());
        response.put("total", accountPage.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/accounts/current")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> saveCurrentAccount(@RequestBody CurrentAccountRequestDTO requestDTO) {
        log.info("Creating current account with: {}", requestDTO);
        try {
            CurrentBankAccountDTO result = bankAccountService.saveCurrentBankAccount(
                    requestDTO.getInitialBalance(),
                    requestDTO.getOverDraft(),
                    requestDTO.getCustomerId());
            return ResponseEntity.ok(result);
        } catch (CustomerNotFoundException e) {
            log.error("Customer not found: {}", requestDTO.getCustomerId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Customer not found with ID: " + requestDTO.getCustomerId());
        } catch (Exception e) {
            log.error("Error creating current account: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating current account: " + e.getMessage());
        }
    }

    @PostMapping("/accounts/saving")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> saveSavingAccount(@RequestBody SavingAccountRequestDTO requestDTO) {
        log.info("Creating saving account with: {}", requestDTO);
        try {
            SavingBankAccountDTO result = bankAccountService.saveSavingBankAccount(
                    requestDTO.getInitialBalance(),
                    requestDTO.getInterestRate(),
                    requestDTO.getCustomerId());
            return ResponseEntity.ok(result);
        } catch (CustomerNotFoundException e) {
            log.error("Customer not found: {}", requestDTO.getCustomerId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Customer not found with ID: " + requestDTO.getCustomerId());
        } catch (Exception e) {
            log.error("Error creating saving account: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating saving account: " + e.getMessage());
        }
    }

    @GetMapping("/accounts/{accountId}/operations")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public List<AccountOperationDTO> getHistory(@PathVariable String accountId) {
        log.info("Fetching history for account: {}", accountId);
        return bankAccountService.accountHistory(accountId);
    }

    @GetMapping("/accounts/{accountId}/pageOperations")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public AccountHistoryDTO getAccountHistory(
            @PathVariable String accountId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size) throws BankAccountNotFoundException {
        log.info("Fetching paged history for account: {}, page: {}, size: {}", accountId, page, size);
        return bankAccountService.getAccountHistory(accountId, page, size);
    }

    @PostMapping("/accounts/debit")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public DebitDTO debit(@RequestBody DebitDTO debitDTO) throws BankAccountNotFoundException, BalanceNotSufficientException {
        log.info("Debiting account: {}", debitDTO);
        this.bankAccountService.debit(debitDTO.getAccountId(), debitDTO.getAmount(), debitDTO.getDescription());
        return debitDTO;
    }

    @PostMapping("/accounts/credit")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public CreditDTO credit(@RequestBody CreditDTO creditDTO) throws BankAccountNotFoundException {
        log.info("Crediting account: {}", creditDTO);
        this.bankAccountService.credit(creditDTO.getAccountId(), creditDTO.getAmount(), creditDTO.getDescription());
        return creditDTO;
    }

    @PostMapping("/accounts/transfer")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public void transfer(@RequestBody TransferRequestDTO transferRequestDTO) throws BankAccountNotFoundException, BalanceNotSufficientException {
        log.info("Transferring: {}", transferRequestDTO);
        this.bankAccountService.transfer(
                transferRequestDTO.getAccountSource(),
                transferRequestDTO.getAccountDestination(),
                transferRequestDTO.getAmount());
    }

    @DeleteMapping("/accounts/{accountId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteBankAccount(@PathVariable String accountId) throws BankAccountNotFoundException {
        log.info("Deleting bank account: {}", accountId);
        bankAccountService.deleteBankAccount(accountId);
    }

    @GetMapping("/accounts/search")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> searchBankAccounts(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Searching bank accounts with keyword: {}, page: {}, size: {}", keyword, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<BankAccountDTO> accountPage = bankAccountService.searchBankAccounts(keyword, pageable);
        Map<String, Object> response = new HashMap<>();
        response.put("accounts", accountPage.getContent());
        response.put("total", accountPage.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/accounts/user")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public List<BankAccountDTO> getUserAccounts() throws CustomerNotFoundException {
        log.info("Fetching accounts for authenticated user");
        return bankAccountService.getUserAccounts();
    }
}