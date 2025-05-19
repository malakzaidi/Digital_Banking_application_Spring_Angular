package org.springmvc.ebanking.web;

import lombok.AllArgsConstructor;
import org.springmvc.ebanking.repositories.AccountOperationRepository;
import org.springmvc.ebanking.repositories.BankAccountRepository;
import org.springmvc.ebanking.repositories.CustomerRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@AllArgsConstructor
public class DashboardController {
    private CustomerRepository customerRepository;
    private BankAccountRepository bankAccountRepository;
    private AccountOperationRepository accountOperationRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCustomers", customerRepository.count());
        stats.put("totalAccounts", bankAccountRepository.count());
        stats.put("totalOperations", accountOperationRepository.count());
        return stats;
    }
}