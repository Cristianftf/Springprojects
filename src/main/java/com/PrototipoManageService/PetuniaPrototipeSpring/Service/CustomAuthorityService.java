package com.PrototipoManageService.PetuniaPrototipeSpring.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PrototipoManageService.PetuniaPrototipeSpring.model.Authority;
import com.PrototipoManageService.PetuniaPrototipeSpring.repository.AuthorityRepository;

import java.util.List;

@Service
public class CustomAuthorityService {

    private final AuthorityRepository authorityRepository;

    public CustomAuthorityService(AuthorityRepository authorityRepository) {
        this.authorityRepository = authorityRepository;
    }

    @Transactional
    public Authority createCustomAuthority(Authority authority) {
        return authorityRepository.save(authority);
    }

    public List<Authority> getCustomAuthorities(String userReferenceId) {
        return authorityRepository.findByUserReferenceId(userReferenceId);
    }

    @Transactional
    public void deleteCustomAuthority(Long id) {
        authorityRepository.deleteById(id);
    }
}