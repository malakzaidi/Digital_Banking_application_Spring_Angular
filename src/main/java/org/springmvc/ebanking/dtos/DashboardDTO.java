package org.springmvc.ebanking.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class DashboardDTO {
    private long totalCustomers;
    private long totalAccounts;
    private double totalBalance;
    private List<AccountOperationDTO> recentTransactions;
    private int currentPage;
    private int pageSize;
    private long totalTransactions;
    private int totalPages;
}