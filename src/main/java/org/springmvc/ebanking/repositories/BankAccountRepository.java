package org.springmvc.ebanking.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springmvc.ebanking.entities.BankAccount;

public interface BankAccountRepository extends JpaRepository <BankAccount, String> {
}
