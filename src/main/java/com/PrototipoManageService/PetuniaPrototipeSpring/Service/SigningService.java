package com.PrototipoManageService.PetuniaPrototipeSpring.Service;

import com.PrototipoManageService.PetuniaPrototipeSpring.dto.SignPdfRequest;
import com.PrototipoManageService.PetuniaPrototipeSpring.model.DigitalIdentity;
import com.PrototipoManageService.PetuniaPrototipeSpring.repository.DigitalIdentityRepository;
import com.PrototipoManageService.PetuniaPrototipeSpring.util.EncryptionUtil;

import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class SigningService {

    private final DigitalIdentityRepository digitalIdentityRepository;
    private final EncryptionUtil encryptionUtil;

    public SigningService(DigitalIdentityRepository digitalIdentityRepository, EncryptionUtil encryptionUtil) {
        this.digitalIdentityRepository = digitalIdentityRepository;
        this.encryptionUtil = encryptionUtil;
    }

    public byte[] signPdf(SignPdfRequest request) {
        // Validar la identidad digital y la contraseña
        Optional<DigitalIdentity> optionalIdentity = digitalIdentityRepository.findById(request.getUuid());
        if (optionalIdentity.isEmpty()) {
            throw new RuntimeException("Identidad digital no encontrada");
        }
        DigitalIdentity identity = optionalIdentity.get();
        String storedPassword = encryptionUtil.decrypt(identity.getEncryptedPassword());
        if (!storedPassword.equals(request.getDigitalidPassword())) {
            throw new RuntimeException("Contraseña incorrecta para la identidad digital");
        }
        // Aquí se integraría la lógica real de firma digital usando PDFBox y BouncyCastle.
        // Para este ejemplo, se devuelve el mismo PDF recibido simulando que se firmó.
        return request.getFile();
    }
}