package org.springmvc.ebanking.dtos;

import lombok.Data;

@Data
public class UserTransactionDTO {
    private double amount;
    private String description;
}