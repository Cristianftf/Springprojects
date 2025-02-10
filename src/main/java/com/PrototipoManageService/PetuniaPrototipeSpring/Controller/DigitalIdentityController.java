package com.PrototipoManageService.PetuniaPrototipeSpring.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.PrototipoManageService.PetuniaPrototipeSpring.Service.DigitalIdentityService;
import com.PrototipoManageService.PetuniaPrototipeSpring.dto.ChangePasswordRequest;
import com.PrototipoManageService.PetuniaPrototipeSpring.dto.GetPkcs12Request;
import com.PrototipoManageService.PetuniaPrototipeSpring.model.DigitalIdentity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/digitalid")
public class DigitalIdentityController {

    private final DigitalIdentityService service;

    public DigitalIdentityController(DigitalIdentityService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DigitalIdentity> createIdentity(@RequestBody CreateDigitalIdentityRequest request) {
        DigitalIdentity identity = new DigitalIdentity();
        identity.setUserReferenceId(request.getUserReferenceId());
        identity.setExpirationDate(request.getExpirationDate());
        identity.setPkcs12File(request.getPkcs12File());
        DigitalIdentity savedIdentity = service.createDigitalIdentity(identity, request.getPlainPassword());
        return new ResponseEntity<>(savedIdentity, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<DigitalIdentity>> getIdentities(@RequestParam String userReferenceId) {
        return ResponseEntity.ok(service.getIdentitiesByUser(userReferenceId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DigitalIdentity> getIdentityById(@PathVariable UUID id) {
        Optional<DigitalIdentity> identity = service.getIdentityById(id);
        return identity.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIdentity(@PathVariable UUID id) {
        service.deleteDigitalIdentity(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoint para recuperar el archivo PKCS12
    @PostMapping("/{id}/pkcs12")
    public ResponseEntity<byte[]> getPkcs12File(@PathVariable UUID id, @RequestBody GetPkcs12Request request) {
        byte[] file = service.retrievePkcs12File(id, request.getDigitalidPassword());
        if (file == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(file);
    }

    // Endpoint para cambiar la contraseña de la identidad digital
    @PutMapping("/{id}/password")
    public ResponseEntity<DigitalIdentity> changePassword(@PathVariable UUID id,
                                                            @RequestBody ChangePasswordRequest request) {
        try {
            DigitalIdentity updatedIdentity = service.updateDigitalIdentityPassword(id, request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok(updatedIdentity);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}