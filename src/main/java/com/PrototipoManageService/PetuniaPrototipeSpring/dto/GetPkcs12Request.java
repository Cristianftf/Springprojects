// Declaración del paquete donde se encuentra la clase
package com.PrototipoManageService.PetuniaPrototipeSpring.dto;

// Importación de la anotación @Data de Lombok para generar automáticamente getters, setters, equals, hashCode y toString
import lombok.Data;

// Anotación que genera automáticamente los métodos mencionados anteriormente
@Data
// Clase que representa una solicitud para obtener un archivo PKCS12
public class GetPkcs12Request {
    // Atributo que almacena la contraseña del certificado digital
    private String digitalidPassword;
}