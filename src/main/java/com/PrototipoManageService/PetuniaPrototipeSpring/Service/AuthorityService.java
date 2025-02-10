package com.PrototipoManageService.PetuniaPrototipeSpring.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.PrototipoManageService.PetuniaPrototipeSpring.model.Authority;
import com.PrototipoManageService.PetuniaPrototipeSpring.repository.AuthorityRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthorityService {

    private final AuthorityRepository authorityRepository;

    public AuthorityService(AuthorityRepository authorityRepository) {
        this.authorityRepository = authorityRepository;
    }

    @Transactional
    public Authority createAuthority(Authority authority) {
        // Aquí se podrían extraer metadatos del certificado
        return authorityRepository.save(authority);
    }

    public List<Authority> getAuthorities() {
        return authorityRepository.findAll();
    }

    @Transactional
    public void deleteAuthority(Long id) {
        authorityRepository.deleteById(id);
    }
}