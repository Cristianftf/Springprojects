// Declaración del paquete donde se encuentra la clase
package com.PrototipoManageService.PetuniaPrototipeSpring.Controller;

// Importación de clases necesarias de Spring Framework para manejo de HTTP y REST
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Importación de clases personalizadas necesarias para el servicio de firma
import com.PrototipoManageService.PetuniaPrototipeSpring.Service.SigningService;
import com.PrototipoManageService.PetuniaPrototipeSpring.dto.SignPdfRequest;

// Anotación que indica que esta clase es un controlador REST
@RestController
// Anotación que define la ruta base para todos los endpoints en este controlador
@RequestMapping("/api/sign")
public class SigningController {

    // Declaración del servicio de firma como dependencia
    private final SigningService signingService;

    // Constructor que inyecta el servicio de firma
    public SigningController(SigningService signingService) {
        this.signingService = signingService;
    }

    // Endpoint POST para firmar PDF
    @PostMapping("/sign-pdf")
    public ResponseEntity<byte[]> signPdf(@RequestBody SignPdfRequest request) {
        try {
            // Llama al servicio para firmar el PDF y almacena el resultado
            byte[] signedPdf = signingService.signPdf(request);
            // Retorna el PDF firmado con estado HTTP 201 (CREATED)
            return ResponseEntity.status(HttpStatus.CREATED).body(signedPdf);
        } catch (RuntimeException ex) {
            // En caso de error, retorna una respuesta HTTP 400 (BAD REQUEST)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}