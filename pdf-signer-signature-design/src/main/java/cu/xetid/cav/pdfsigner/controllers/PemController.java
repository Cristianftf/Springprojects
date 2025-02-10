package cu.xetid.cav.pdfsigner.controllers; // Declara el paquete del controlador

// Importaciones necesarias para el funcionamiento
import java.security.cert.X509Certificate;
import java.util.Map;
import javax.security.auth.x500.X500Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import cu.xetid.cav.pdfsigner.validation.PemValidator;

@RestController // Indica que es un controlador REST
@RequestMapping("pem") // Define la ruta base del controlador
@CrossOrigin(origins = "*") // Permite peticiones desde cualquier origen
public class PemController {
  @PostMapping("/parse") // Define el endpoint para procesar archivos PEM
  public ResponseEntity<?> parsePem(@RequestPart("pem") MultipartFile pemFile) { // Método que recibe un archivo PEM
    var pemValidator = new PemValidator(); // Crea una instancia del validador PEM
    try {
      X509Certificate cert = pemValidator.parse(pemFile.getInputStream()); // Parsea el archivo PEM a certificado X509
      X500Principal subject = cert.getSubjectX500Principal(); // Obtiene el sujeto del certificado
      X500Principal issuer = cert.getIssuerX500Principal(); // Obtiene el emisor del certificado
      // log.debug(first.getNotBefore().toString()); // Línea comentada para debug
      // log.debug(first.getNotAfter().toString()); // Línea comentada para debug
      // log.debug(first.getSubjectX500Principal().getName()); // Línea comentada para debug
      // log.debug(first.getIssuerX500Principal().getName()); // Línea comentada para debug
      return ResponseEntity.ok( // Retorna respuesta exitosa
        Map.ofEntries( // Crea un mapa con la información del certificado
          Map.entry("isValid", true), // Indica que el certificado es válido
          Map.entry("notBefore", cert.getNotBefore()), // Fecha de inicio de validez
          Map.entry("notAfter", cert.getNotAfter()), // Fecha de fin de validez
          Map.entry("subject", subject.getName()), // Nombre del sujeto
          Map.entry("issuer", issuer.getName()) // Nombre del emisor
        )
      );
    } catch (Exception e) { // Manejo de errores
      return ResponseEntity.status(500).body( // Retorna respuesta de error
        Map.ofEntries(Map.entry("isValid", false), Map.entry("message", e.getMessage())) // Indica error y mensaje
      );
    }
  }
}