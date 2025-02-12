package com.PrototipoManageService.PetuniaPrototipeSpring.dto; // Define el paquete donde se encuentra la clase

import lombok.Data; // Importa la anotación @Data de Lombok para generar automáticamente getters, setters, equals, hashCode y toString
import java.util.UUID; // Importa la clase UUID para manejar identificadores únicos universales

@Data // Anotación que genera automáticamente los métodos mencionados anteriormente
public class SignPdfRequest { // Clase que representa la solicitud para firmar un PDF
    private UUID uuid; // Identificador único de la identidad digital
    private String digitalidPassword; // Contraseña de la identidad digital
    private byte[] file; // Archivo PDF en formato binario que se va a firmar

    // Campos opcionales para personalizar la firma:
    private String signerEntity; // Nombre de la entidad que firma
    private String signerJobTitle; // Cargo o título del firmante
    private Integer signatureWidth; // Ancho de la firma en el documento
    private Integer signatureHeight; // Alto de la firma en el documento
    private Integer signaturePositionX; // Posición X de la firma en el documento
    private Integer signaturePositionY; // Posición Y de la firma en el documento
    private String signatureDescription; // Descripción o texto adicional de la firma
    // Se pueden agregar más parámetros según sea necesario
}