package com.PrototipoManageService.PetuniaPrototipeSpring.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.PrototipoManageService.PetuniaPrototipeSpring.Service.CustomAuthorityService;
import com.PrototipoManageService.PetuniaPrototipeSpring.model.Authority;

import java.util.List;

@RestController
@RequestMapping("/api/custom-ac")
public class CustomAuthorityController {

    private final CustomAuthorityService customAuthorityService;

    public CustomAuthorityController(CustomAuthorityService customAuthorityService) {
        this.customAuthorityService = customAuthorityService;
    }

    @PostMapping
    public ResponseEntity<Authority> createCustomAuthority(@RequestBody Authority authority) {
        Authority saved = customAuthorityService.createCustomAuthority(authority);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<Authority>> getCustomAuthorities(@RequestParam String userReferenceId) {
        return ResponseEntity.ok(customAuthorityService.getCustomAuthorities(userReferenceId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomAuthority(@PathVariable Long id) {
        customAuthorityService.deleteCustomAuthority(id);
        return ResponseEntity.noContent().build();
    }
}
