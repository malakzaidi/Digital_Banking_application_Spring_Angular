package org.springmvc.ebanking.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springmvc.ebanking.dtos.DashboardDTO;
import org.springmvc.ebanking.services.BankAccountsService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@AllArgsConstructor
@Slf4j
public class DashboardController {

    private final BankAccountsService bankAccountService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboard(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size) {
        log.info("Fetching dashboard data, page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        DashboardDTO dashboard = bankAccountService.getDashboardData(pageable);
        Map<String, Object> response = new HashMap<>();
        response.put("totalCustomers", dashboard.getTotalCustomers());
        response.put("totalAccounts", dashboard.getTotalAccounts());
        response.put("recentTransactions", dashboard.getRecentTransactions());
        response.put("totalBalance", dashboard.getTotalBalance());
        return ResponseEntity.ok(response);
    }
}