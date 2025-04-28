package org.springmvc.ebanking.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springmvc.ebanking.entities.AccountOperation;

public interface AccountOperationRepository extends JpaRepository<AccountOperation, Long> {
}
