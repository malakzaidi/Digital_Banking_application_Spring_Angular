package org.springmvc.ebanking.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springmvc.ebanking.dtos.DashboardDTO;
import org.springmvc.ebanking.exceptions.CustomerNotFoundException;
import org.springmvc.ebanking.services.BankAccountsService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@AllArgsConstructor
@Slf4j
public class DashboardController {

    private final BankAccountsService bankAccountService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboard(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size) {
        log.info("Fetching dashboard data, page: {}, size: {}", page, size);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth != null ? auth.getName() : "unknown";
        Pageable pageable = PageRequest.of(page, size);
        try {
            DashboardDTO dashboard = bankAccountService.getDashboardData(userId, pageable);
            Map<String, Object> response = new HashMap<>();
            response.put("totalCustomers", dashboard.getTotalCustomers());
            response.put("totalAccounts", dashboard.getTotalAccounts());
            response.put("totalBalance", dashboard.getTotalBalance());
            response.put("recentTransactions", dashboard.getRecentTransactions());
            response.put("currentPage", dashboard.getCurrentPage());
            response.put("pageSize", dashboard.getPageSize());
            response.put("totalTransactions", dashboard.getTotalTransactions());
            response.put("totalPages", dashboard.getTotalPages());
            return ResponseEntity.ok(response);
        } catch (CustomerNotFoundException e) {
            log.error("Customer not found for user: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Customer not found for user: " + userId));
        } catch (Exception e) {
            log.error("Error fetching dashboard data: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error fetching dashboard data: " + e.getMessage()));
        }
    }
}