package com.PrototipoManageService.PetuniaPrototipeSpring.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.PrototipoManageService.PetuniaPrototipeSpring.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
}
