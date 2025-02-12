package com.PrototipoManageService.PetuniaPrototipeSpring.dto; // Define el paquete donde se encuentra la clase

import lombok.Data; // Importa la anotación @Data de Lombok para generar automáticamente getters, setters, equals, hashCode y toString
import java.time.LocalDateTime; // Importa la clase para manejar fechas y horas

@Data // Anotación que genera automáticamente los métodos mencionados anteriormente
public class CreateDigitalIdentityRequest { // Clase que representa la solicitud para crear una identidad digital
    private String userReferenceId; // Identificador de referencia del usuario
    private String plainPassword; // Contraseña en texto claro
    private LocalDateTime expirationDate; // Fecha de expiración de la identidad digital
    private byte[] pkcs12File; // Archivo PKCS12 (en binario o base64 decodificado)
}