package com.PrototipoManageService.PetuniaPrototipeSpring.Controller;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateDigitalIdentityRequest {
    private String userReferenceId;
    private String plainPassword; // Contraseña en texto claro que se cifrará
    private LocalDateTime expirationDate;
    private byte[] pkcs12File; // El archivo p12 en formato binario
}
