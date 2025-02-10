// Declaración del paquete al que pertenece la clase
package cu.xetid.cav.pdfsigner.controllers;

// Importación de clases necesarias
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cu.xetid.cav.pdfsigner.util.RSA;

// Anotación que indica que es un controlador REST
@RestController
// Anotación que define la ruta base para todas las peticiones del controlador
@RequestMapping("security")
// Anotación que permite peticiones desde cualquier origen (CORS)
@CrossOrigin(origins = "*")
public class SecurityController {
  // Método que maneja las peticiones GET a la ruta /get-public-key
  @GetMapping("/get-public-key")
  public ResponseEntity<?> getPublicKey() {
    try {
      // Obtiene la clave pública usando el singleton de RSA
      String publicKey = RSA.getInstance().getPublicKey();
      // Retorna la clave pública con estado 200 OK
      return ResponseEntity.ok(publicKey);
    } catch (Exception e) {
      // En caso de error, retorna un estado 500 con el mensaje de error
      return ResponseEntity.status(500).body(Map.entry("message", e.getMessage()));
    }
  } 
}