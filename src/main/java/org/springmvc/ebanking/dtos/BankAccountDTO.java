package org.springmvc.ebanking.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;



@Data

public class BankAccountDTO {
    private String type;
    private String id;

    public void setId(String id) {

    }

    public void setBalance(double balance) {
    }

    public void setCustomerId(Long aLong) {
    }

    public void setCreatedAt(String string) {
    }

    public String getId() {
        return id ;
    }
}
