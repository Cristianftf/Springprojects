package com.PrototipoManageService.PetuniaPrototipeSpring.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.PrototipoManageService.PetuniaPrototipeSpring.model.DigitalIdentity;

import java.util.List;
import java.util.UUID;

public interface DigitalIdentityRepository extends JpaRepository<DigitalIdentity, UUID> {
    List<DigitalIdentity> findByUserReferenceId(String userReferenceId);
}
