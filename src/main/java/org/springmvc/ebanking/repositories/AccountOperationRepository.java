package org.springmvc.ebanking.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springmvc.ebanking.entities.AccountOperation;

import java.util.List;

public interface AccountOperationRepository extends JpaRepository<AccountOperation,Long> {
    List<AccountOperation> findByBankAccountId(String accountId);
    Page<AccountOperation> findByBankAccountIdOrderByOperationDateDesc(String accountId, Pageable pageable);

    Page<AccountOperation> findByBankAccountIdInOrderByOperationDateDesc(List<String> accountIds, Pageable pageable);

    Page<AccountOperation> findAllByOrderByOperationDateDesc(Pageable pageable);

    Page<AccountOperation> findByBankAccountIdIn(List<String> bankAccountIds, Pageable pageable);

    Page<AccountOperation> findByBankAccountId(String accountId, PageRequest of);
}
