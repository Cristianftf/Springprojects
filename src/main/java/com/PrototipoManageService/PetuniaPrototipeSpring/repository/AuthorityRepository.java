package com.PrototipoManageService.PetuniaPrototipeSpring.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.PrototipoManageService.PetuniaPrototipeSpring.model.Authority;

import java.util.List;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {
    // Para obtener autoridades personalizadas de un usuario
    List<Authority> findByUserReferenceId(String userReferenceId);
}