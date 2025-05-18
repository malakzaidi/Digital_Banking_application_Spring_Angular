package org.springmvc.ebanking.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springmvc.ebanking.entities.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}