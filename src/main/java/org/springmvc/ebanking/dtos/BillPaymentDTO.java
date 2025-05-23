package org.springmvc.ebanking.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class BillPaymentDTO {
    private String accountId;
    private String billerName;
    private double amount;
    private String userId;
    private String description;

}