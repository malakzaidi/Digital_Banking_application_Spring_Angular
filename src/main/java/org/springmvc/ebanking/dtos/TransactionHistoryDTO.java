package org.springmvc.ebanking.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Getter
@Setter
public class TransactionHistoryDTO {
    private Long id;
    private String accountId;
    private String type;
    private double amount;
    private Date date;
    private String performedBy;
    private String description;
}
