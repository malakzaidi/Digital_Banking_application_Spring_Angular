package org.springmvc.ebanking.repositories;

import org.springmvc.ebanking.entities.BankAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BankAccountRepository extends JpaRepository<BankAccount, String> {

    List<BankAccount> findByCustomerId(Long customerId);

    @Query("SELECT ba FROM BankAccount ba JOIN ba.customer c WHERE ba.id LIKE %:keyword% OR cast(c.id as string) LIKE %:keyword% OR c.name LIKE %:keyword%")
    Page<BankAccount> findByIdContainingOrCustomerNameContaining(@Param("keyword") String keyword, Pageable pageable);
}