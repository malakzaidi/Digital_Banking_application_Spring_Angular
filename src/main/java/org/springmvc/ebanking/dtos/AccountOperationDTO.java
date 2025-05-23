package org.springmvc.ebanking.dtos;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springmvc.ebanking.enums.OperationType;

import java.util.Date;

@Data
@Getter
@Setter
public class AccountOperationDTO {
    private Long id;
    private Date operationDate;
    private double amount;
    private String description;
    private OperationType type;
    private String accountId;
    private String performedBy; // Username of the user who performed the operation
}