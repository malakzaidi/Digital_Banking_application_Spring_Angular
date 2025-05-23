package org.springmvc.ebanking.dtos;


import lombok.Data;

@Data
public class UserTransferDTO {
    private String recipientIdentifier; // Email or username
    private double amount;
}