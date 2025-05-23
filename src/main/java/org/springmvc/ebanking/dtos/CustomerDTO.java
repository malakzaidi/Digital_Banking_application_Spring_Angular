package org.springmvc.ebanking.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CustomerDTO {
    private Long id;
    private String name;
    private String email;
    private String createdBy;
    private String updatedBy;
}