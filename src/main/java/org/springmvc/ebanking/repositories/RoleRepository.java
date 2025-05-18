package org.springmvc.ebanking.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springmvc.ebanking.entities.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    Boolean existsByName(String name);
}