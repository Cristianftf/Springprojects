// Declaración del paquete donde se encuentra la clase
package com.PrototipoManageService.PetuniaPrototipeSpring.Controller;

// Importación de clases necesarias de Spring Framework y otras dependencias
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.PrototipoManageService.PetuniaPrototipeSpring.Service.VerifyService;
import com.PrototipoManageService.PetuniaPrototipeSpring.dto.VerifyPdfResponse;

// Anotación que indica que esta clase es un controlador REST
@RestController
// Anotación que define la ruta base para todos los endpoints en este controlador
@RequestMapping("/api/verify")
public class VerifyController {

    // Declaración del servicio que maneja la lógica de verificación
    private final VerifyService verifyService;

    // Constructor que inyecta el servicio de verificación
    public VerifyController(VerifyService verifyService) {
        this.verifyService = verifyService;
    }

    // Endpoint POST para verificar archivos PDF
    @PostMapping("/verify-pdf")
    public ResponseEntity<VerifyPdfResponse> verifyPdf(@RequestParam("file") MultipartFile file,
                                                       @RequestParam(value = "userCid", required = false) String userCid,
                                                       @RequestParam(value = "userId", required = false) String userId) {
        // Llama al servicio para verificar el PDF y almacena la respuesta
        VerifyPdfResponse response = verifyService.verifyPdf(file);
        // Retorna la respuesta con estado HTTP 201 (CREATED) y el cuerpo de la respuesta
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}