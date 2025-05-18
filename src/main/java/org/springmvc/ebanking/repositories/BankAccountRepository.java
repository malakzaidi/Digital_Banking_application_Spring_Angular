package org.springmvc.ebanking.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springmvc.ebanking.entities.BankAccount;

import java.util.List;

public interface BankAccountRepository extends JpaRepository <BankAccount, String> {
    List<BankAccount> findByCustomerId(Long customerId);

    @Query("SELECT b FROM BankAccount b WHERE LOWER(b.id) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR b.customer.id = :customerId " +
            "OR LOWER(b.customer.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<BankAccount> findByIdOrCustomerIdOrCustomerName(@Param("keyword") String keyword, @Param("customerId") Long customerId);

    default List<BankAccount> findByIdOrCustomerIdOrCustomerName(String keyword) {
        Long customerId;
        try {
            customerId = Long.parseLong(keyword);
        } catch (NumberFormatException e) {
            customerId = -1L; // Invalid ID, won't match any customer
        }
        return findByIdOrCustomerIdOrCustomerName(keyword, customerId);
    }
}
