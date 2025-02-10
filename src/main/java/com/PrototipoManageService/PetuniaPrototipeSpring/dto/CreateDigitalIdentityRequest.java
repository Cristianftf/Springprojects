package com.PrototipoManageService.PetuniaPrototipeSpring.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateDigitalIdentityRequest {
    private String userReferenceId;
    private String plainPassword; // Contraseña en texto claro
    private LocalDateTime expirationDate;
    private byte[] pkcs12File; // Archivo PKCS12 (en binario o base64 decodificado)
}
