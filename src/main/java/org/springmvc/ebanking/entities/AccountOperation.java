package org.springmvc.ebanking.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springmvc.ebanking.enums.OperationType;

import java.util.Date;
@Entity
@Data @NoArgsConstructor @AllArgsConstructor
@Getter
@Setter
public class AccountOperation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date operationDate;
    private double amount;
    @Enumerated(EnumType.STRING)
    private OperationType type;
    @ManyToOne
    private BankAccount bankAccount;
    private String description;

    // Added to track which user performed the operation
    @ManyToOne
    @JoinColumn(name = "performed_by")
    private User performedBy;


}
