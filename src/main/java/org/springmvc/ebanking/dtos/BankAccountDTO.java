package org.springmvc.ebanking.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springmvc.ebanking.enums.AccountStatus;

import java.util.Date;

@Getter
@Setter
@Data
public class BankAccountDTO {
    private String id;
    private double balance;
    private Date createdAt;
    private Long customerId;
    private String customerName;
    private String type;
    private AccountStatus status;
    private String createdBy;
    private String updatedBy;
    private Date updatedAt;
}