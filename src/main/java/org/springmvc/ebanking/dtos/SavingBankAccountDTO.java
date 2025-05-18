package org.springmvc.ebanking.dtos;

import lombok.Data;
import org.springmvc.ebanking.enums.AccountStatus;

import java.util.Date;
@Data
public class SavingBankAccountDTO extends BankAccountDTO {
    private String id;
    private double balance;
    private Date createdAt;
    private AccountStatus status;
    private CustomerDTO customerDTO;
    private double interestRate;
}