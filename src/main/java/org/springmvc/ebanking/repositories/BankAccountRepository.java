package org.springmvc.ebanking.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springmvc.ebanking.entities.BankAccount;

import java.util.List;

public interface BankAccountRepository extends JpaRepository <BankAccount, String> {
    List<BankAccount> findByCustomerId(Long customerId);
}
