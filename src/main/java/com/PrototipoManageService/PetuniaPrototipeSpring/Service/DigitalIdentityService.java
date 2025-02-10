package com.PrototipoManageService.PetuniaPrototipeSpring.Service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PrototipoManageService.PetuniaPrototipeSpring.model.DigitalIdentity;
import com.PrototipoManageService.PetuniaPrototipeSpring.repository.DigitalIdentityRepository;
import com.PrototipoManageService.PetuniaPrototipeSpring.util.EncryptionUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DigitalIdentityService {

    private final DigitalIdentityRepository repository;
    private final EncryptionUtil encryptionUtil;

    public DigitalIdentityService(DigitalIdentityRepository repository, EncryptionUtil encryptionUtil) {
        this.repository = repository;
        this.encryptionUtil = encryptionUtil;
    }

    @Transactional
    public DigitalIdentity createDigitalIdentity(DigitalIdentity identity, String plainPassword) {
        String encrypted = encryptionUtil.encrypt(plainPassword);
        identity.setEncryptedPassword(encrypted);
        return repository.save(identity);
    }

    public List<DigitalIdentity> getIdentitiesByUser(String userReferenceId) {
        return repository.findByUserReferenceId(userReferenceId);
    }

    public Optional<DigitalIdentity> getIdentityById(UUID id) {
        return repository.findById(id);
    }

    @Transactional
    public void deleteDigitalIdentity(UUID id) {
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public byte[] retrievePkcs12File(UUID id, String providedPassword) {
        Optional<DigitalIdentity> optionalIdentity = repository.findById(id);
        if (optionalIdentity.isPresent()) {
            DigitalIdentity identity = optionalIdentity.get();
            // Desencriptar y comparar la contraseña
            String storedPassword = encryptionUtil.decrypt(identity.getEncryptedPassword());
            if (storedPassword.equals(providedPassword)) {
                return identity.getPkcs12File();
            }
        }
        return null;
    }

    @Transactional
    public DigitalIdentity updateDigitalIdentityPassword(UUID id, String oldPlainPassword, String newPlainPassword) {
        Optional<DigitalIdentity> optionalIdentity = repository.findById(id);
        if (optionalIdentity.isPresent()) {
            DigitalIdentity identity = optionalIdentity.get();
            String storedPassword = encryptionUtil.decrypt(identity.getEncryptedPassword());
            if (!storedPassword.equals(oldPlainPassword)) {
                throw new RuntimeException("La contraseña antigua no coincide.");
            }
            String encryptedNew = encryptionUtil.encrypt(newPlainPassword);
            identity.setEncryptedPassword(encryptedNew);
            return repository.save(identity);
        } else {
            throw new RuntimeException("Identidad digital no encontrada");
        }
    }
}