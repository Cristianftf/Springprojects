// Declaración del paquete donde se encuentra la clase
package com.PrototipoManageService.PetuniaPrototipeSpring.dto;

// Importación de la anotación @Data de Lombok para generar automáticamente getters, setters, equals, hashCode y toString
import lombok.Data;

// Anotación que genera automáticamente los métodos mencionados anteriormente
@Data
// Clase que representa una solicitud de cambio de contraseña
public class ChangePasswordRequest {
    // Campo que almacena la contraseña anterior del usuario
    private String oldPassword;
    // Campo que almacena la nueva contraseña del usuario
    private String newPassword;
}