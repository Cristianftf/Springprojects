package com.PrototipoManageService.PetuniaPrototipeSpring.Controller; // Define el paquete donde se encuentra la clase

import lombok.Data; // Importa la anotación @Data de Lombok para generar automáticamente getters, setters, equals, hashCode y toString

import java.time.LocalDateTime; // Importa la clase para manejar fechas y horas

@Data // Anotación que genera automáticamente los métodos mencionados anteriormente
public class CreateDigitalIdentityRequest { // Clase que representa la estructura de una solicitud para crear una identidad digital
    private String userReferenceId; // Identificador de referencia del usuario
    private String plainPassword; // Contraseña en texto claro que se cifrará
    private LocalDateTime expirationDate; // Fecha y hora de expiración de la identidad digital
    private byte[] pkcs12File; // El archivo p12 en formato binario (certificado digital)
}