package org.springmvc.ebanking.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springmvc.ebanking.entities.Customer;
import org.springmvc.ebanking.entities.User;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer,Long> {
    @Query("select c from Customer c where c.name like :kw")
    List<Customer> searchCustomer(@Param("kw") String keyword);
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByUser(User user);
    List<Customer> findByNameContains(String keyword);

}