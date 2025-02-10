// Declaración del paquete donde se encuentra la clase
package cu.xetid.cav.pdfsigner.controllers;

// Importación de clases necesarias para el funcionamiento
import cu.xetid.cav.pdfsigner.validation.cert.CaCertificatesStore;
import io.swagger.v3.oas.annotations.Hidden;

import java.io.IOException;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Anotación para habilitar el logging usando Slf4j
@Slf4j
// Anotación para ocultar el controlador en la documentación de Swagger
@Hidden
// Anotación que marca la clase como un controlador REST
@RestController
// Anotación que define la ruta base para todos los endpoints
@RequestMapping("/")
// Anotación que permite peticiones desde cualquier origen (CORS)
@CrossOrigin(origins = "*")
public class AppController {
  // Endpoint GET que responde a la ruta raíz
  @GetMapping("/")
  public ResponseEntity<String> hello() {
    // Retorna una respuesta HTTP 200 con el mensaje "hello"
    return ResponseEntity.ok("hello");
  }

  // Endpoint POST para refrescar los certificados de confianza por defecto
  @PostMapping("/refresh-default-trusted-certs")
  public ResponseEntity<?> refresh() throws IOException {
    try {
      // Obtiene la instancia del almacén de certificados y establece los certificados de confianza por defecto
      CaCertificatesStore.getInstance().setDefaultTrustedCertificates();
      // Retorna una respuesta HTTP 200 con el mensaje "ok"
      return ResponseEntity.ok("ok");
    } catch (Exception e) {
      // Registra el error en el log
      log.error(e.getMessage());
      // Retorna una respuesta HTTP 500 con el mensaje de error
      return ResponseEntity.status(500).body(Map.entry("message", e.getMessage()));
    }
  }
}