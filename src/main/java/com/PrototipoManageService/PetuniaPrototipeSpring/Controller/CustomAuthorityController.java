// Declaración del paquete donde se encuentra la clase
package com.PrototipoManageService.PetuniaPrototipeSpring.Controller;

// Importación de clases necesarias de Spring Framework y otras dependencias
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.PrototipoManageService.PetuniaPrototipeSpring.Service.CustomAuthorityService;
import com.PrototipoManageService.PetuniaPrototipeSpring.model.Authority;

import java.util.List;

// Anotación que indica que esta clase es un controlador REST
@RestController
// Define la ruta base para todos los endpoints en este controlador
@RequestMapping("/api/custom-ac")
public class CustomAuthorityController {

    // Declaración del servicio que maneja la lógica de negocio
    private final CustomAuthorityService customAuthorityService;

    // Constructor que inyecta el servicio
    public CustomAuthorityController(CustomAuthorityService customAuthorityService) {
        this.customAuthorityService = customAuthorityService;
    }

    // Endpoint POST para crear una nueva autoridad personalizada
    @PostMapping
    public ResponseEntity<Authority> createCustomAuthority(@RequestBody Authority authority) {
        // Llama al servicio para crear la autoridad y la guarda
        Authority saved = customAuthorityService.createCustomAuthority(authority);
        // Retorna una respuesta con estado CREATED y la autoridad guardada
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Endpoint GET para obtener todas las autoridades de un usuario específico
    @GetMapping
    public ResponseEntity<List<Authority>> getCustomAuthorities(@RequestParam String userReferenceId) {
        // Retorna una lista de autoridades asociadas al ID de referencia del usuario
        return ResponseEntity.ok(customAuthorityService.getCustomAuthorities(userReferenceId));
    }

    // Endpoint DELETE para eliminar una autoridad específica por su ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomAuthority(@PathVariable Long id) {
        // Llama al servicio para eliminar la autoridad
        customAuthorityService.deleteCustomAuthority(id);
        // Retorna una respuesta sin contenido indicando éxito
        return ResponseEntity.noContent().build();
    }
}