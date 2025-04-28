package org.springmvc.ebanking.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springmvc.ebanking.entities.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
