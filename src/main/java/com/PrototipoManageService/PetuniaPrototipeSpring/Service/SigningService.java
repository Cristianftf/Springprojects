// Paquete donde se encuentra la clase
package com.PrototipoManageService.PetuniaPrototipeSpring.Service;

// Importaciones necesarias para el funcionamiento del servicio
import com.PrototipoManageService.PetuniaPrototipeSpring.dto.SignPdfRequest;
import com.PrototipoManageService.PetuniaPrototipeSpring.model.DigitalIdentity;
import com.PrototipoManageService.PetuniaPrototipeSpring.repository.DigitalIdentityRepository;
import com.PrototipoManageService.PetuniaPrototipeSpring.util.EncryptionUtil;

import org.springframework.stereotype.Service;
import java.util.Optional;

// Anotación que indica que esta clase es un servicio de Spring
@Service
public class SigningService {

    // Repositorio para acceder a las identidades digitales
    private final DigitalIdentityRepository digitalIdentityRepository;
    // Utilidad para el manejo de encriptación
    private final EncryptionUtil encryptionUtil;

    // Constructor que inyecta las dependencias necesarias
    public SigningService(DigitalIdentityRepository digitalIdentityRepository, EncryptionUtil encryptionUtil) {
        this.digitalIdentityRepository = digitalIdentityRepository;
        this.encryptionUtil = encryptionUtil;
    }

    // Método para firmar un PDF
    public byte[] signPdf(SignPdfRequest request) {
        // Busca la identidad digital por UUID
        Optional<DigitalIdentity> optionalIdentity = digitalIdentityRepository.findById(request.getUuid());
        // Verifica si la identidad existe
        if (optionalIdentity.isEmpty()) {
            throw new RuntimeException("Identidad digital no encontrada");
        }
        // Obtiene la identidad digital
        DigitalIdentity identity = optionalIdentity.get();
        // Desencripta la contraseña almacenada
        String storedPassword = encryptionUtil.decrypt(identity.getEncryptedPassword());
        // Verifica si la contraseña proporcionada coincide con la almacenada
        if (!storedPassword.equals(request.getDigitalidPassword())) {
            throw new RuntimeException("Contraseña incorrecta para la identidad digital");
        }
        // Aquí se integraría la lógica real de firma digital usando PDFBox y BouncyCastle.
        // Para este ejemplo, se devuelve el mismo PDF recibido simulando que se firmó.
        return request.getFile();
    }
}