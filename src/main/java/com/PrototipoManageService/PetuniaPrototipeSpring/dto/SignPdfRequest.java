package com.PrototipoManageService.PetuniaPrototipeSpring.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class SignPdfRequest {
    private UUID uuid; // Identificador de la identidad digital
    private String digitalidPassword;
    private byte[] file; // Archivo PDF en binario

    // Opcionales:
    private String signerEntity;
    private String signerJobTitle;
    private Integer signatureWidth;
    private Integer signatureHeight;
    private Integer signaturePositionX;
    private Integer signaturePositionY;
    private String signatureDescription;
    // Se pueden agregar más parámetros según sea necesario
}
