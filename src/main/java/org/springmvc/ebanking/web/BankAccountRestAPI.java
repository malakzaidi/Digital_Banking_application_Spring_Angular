package org.springmvc.ebanking.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    // ADMIN-ONLY: For in-agency account creation
    @PostMapping("/accounts/current")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> saveCurrentAccount(@RequestBody CurrentAccountRequestDTO requestDTO) {
        log.info("Admin creating current account with: {}", requestDTO);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth != null ? auth.getName() : "unknown";
        log.info("Authenticated admin: {}, authorities: {}", userId, auth != null ? auth.getAuthorities() : "none");
        try {
            CurrentBankAccountDTO result = bankAccountService.saveCurrentBankAccount(
                    requestDTO.getInitialBalance(),
                    requestDTO.getOverDraft(),
                    requestDTO.getCustomerId(),
                    userId);
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

    // ADMIN-ONLY: For in-agency account creation
    @PostMapping("/accounts/saving")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> saveSavingAccount(@RequestBody SavingAccountRequestDTO requestDTO) {
        log.info("Admin creating saving account with: {}", requestDTO);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth != null ? auth.getName() : "unknown";
        log.info("Authenticated admin: {}, authorities: {}", userId, auth != null ? auth.getAuthorities() : "none");
        try {
            SavingBankAccountDTO result = bankAccountService.saveSavingBankAccount(
                    requestDTO.getInitialBalance(),
                    requestDTO.getInterestRate(),
                    requestDTO.getCustomerId(),
                    userId);
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

    // USER-ONLY: Simplified account creation for regular users
    @PostMapping("/user/accounts/new")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> createUserAccount(@RequestBody UserAccountRequestDTO requestDTO) {
        log.info("User creating new account with initial balance: {}", requestDTO.getInitialBalance());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth != null ? auth.getName() : "unknown";
        log.info("Authenticated user: {}, authorities: {}", userId, auth != null ? auth.getAuthorities() : "none");
        try {
            BankAccountDTO result = bankAccountService.createUserAccount(
                    requestDTO.getInitialBalance(),
                    userId);
            return ResponseEntity.ok(result);
        } catch (CustomerNotFoundException e) {
            log.error("User not found: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found: " + userId);
        } catch (Exception e) {
            log.error("Error creating user account: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating account: " + e.getMessage());
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

    // ADMIN-ONLY: For in-agency transactions
    @PostMapping("/accounts/debit")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public DebitDTO debit(@RequestBody DebitDTO debitDTO) throws BankAccountNotFoundException, BalanceNotSufficientException {
        log.info("Admin debiting account: {}", debitDTO);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth != null ? auth.getName() : "unknown";
        this.bankAccountService.debit(debitDTO.getAccountId(), debitDTO.getAmount(), debitDTO.getDescription(), userId);
        return debitDTO;
    }

    // ADMIN-ONLY: For in-agency transactions
    @PostMapping("/accounts/credit")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public CreditDTO credit(@RequestBody CreditDTO creditDTO) throws BankAccountNotFoundException {
        log.info("Admin crediting account: {}", creditDTO);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth != null ? auth.getName() : "unknown";
        this.bankAccountService.credit(creditDTO.getAccountId(), creditDTO.getAmount(), creditDTO.getDescription(), userId);
        return creditDTO;
    }

    // USER-ONLY: Simplified debit for users
    @PostMapping("/user/transactions/debit")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> userDebit(@RequestBody UserTransactionDTO transactionDTO) throws BankAccountNotFoundException, BalanceNotSufficientException, CustomerNotFoundException {
        log.info("User debiting with: {}", transactionDTO);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth != null ? auth.getName() : "unknown";
        bankAccountService.userDebit(userId, transactionDTO.getAmount(), transactionDTO.getDescription());
        return ResponseEntity.ok("Debit successful");
    }

    // USER-ONLY: Simplified credit for users
    @PostMapping("/user/transactions/credit")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> userCredit(@RequestBody UserTransactionDTO transactionDTO) throws BankAccountNotFoundException, CustomerNotFoundException {
        log.info("User crediting with: {}", transactionDTO);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth != null ? auth.getName() : "unknown";
        bankAccountService.userCredit(userId, transactionDTO.getAmount(), transactionDTO.getDescription());
        return ResponseEntity.ok("Credit successful");
    }

    // ADMIN-ONLY: For in-agency transfers
    @PostMapping("/accounts/transfer")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void transfer(@RequestBody TransferRequestDTO transferRequestDTO) throws BankAccountNotFoundException, BalanceNotSufficientException {
        log.info("Admin transferring: {}", transferRequestDTO);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth != null ? auth.getName() : "unknown";
        this.bankAccountService.transfer(
                transferRequestDTO.getAccountSource(),
                transferRequestDTO.getAccountDestination(),
                transferRequestDTO.getAmount(),
                userId);
    }

    // USER-ONLY: Simplified transfer for users using recipient email/username
    @PostMapping("/user/transfers")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> userTransfer(@RequestBody UserTransferDTO transferDTO) throws BankAccountNotFoundException, BalanceNotSufficientException, CustomerNotFoundException {
        log.info("User transferring: {}", transferDTO);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth != null ? auth.getName() : "unknown";
        bankAccountService.userTransfer(userId, transferDTO.getRecipientIdentifier(), transferDTO.getAmount());
        return ResponseEntity.ok("Transfer successful");
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth != null ? auth.getName() : "unknown";
        return bankAccountService.getUserAccounts(userId);
    }

    @PostMapping("/bills/pay")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> payBill(@RequestBody BillPaymentDTO billPaymentDTO) throws BankAccountNotFoundException, BalanceNotSufficientException {
        log.info("Paying bill with: {}", billPaymentDTO);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth != null ? auth.getName() : "unknown";
        billPaymentDTO.setUserId(userId);
        bankAccountService.payBill(billPaymentDTO);
        return ResponseEntity.ok("Bill paid successfully");
    }

    @GetMapping("/transactions/history")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<List<TransactionHistoryDTO>> getTransactionHistory(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) throws CustomerNotFoundException {
        log.info("Fetching transaction history, page: {}, size: {}", page, size);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth != null ? auth.getName() : "unknown";
        Pageable pageable = PageRequest.of(page, size);
        List<TransactionHistoryDTO> history = bankAccountService.getTransactionHistory(userId, pageable);
        return ResponseEntity.ok(history);
    }
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public DashboardDTO getDashboardData(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size) {
        log.info("Fetching dashboard data, page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return bankAccountService.getDashboardData(pageable);
    }
}