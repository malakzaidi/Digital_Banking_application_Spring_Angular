package org.springmvc.ebanking.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class BankAccountDTO {
    private String type;

    public void setId(String id) {

    }

    public void setBalance(double balance) {
    }

    public void setCustomerId(Long aLong) {
    }

    public void setCreatedAt(String string) {
    }
}