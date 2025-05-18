package org.springmvc.ebanking.dtos;

import lombok.Data;

@Data
    public class SavingAccountRequestDTO {
        private double initialBalance;
        private double interestRate;
        private Long customerId;
    }
