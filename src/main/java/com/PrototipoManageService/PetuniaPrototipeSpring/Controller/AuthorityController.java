// Declaración del paquete donde se encuentra la clase
package com.PrototipoManageService.PetuniaPrototipeSpring.Controller;

// Importación de clases necesarias de Spring Framework y otras dependencias
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.PrototipoManageService.PetuniaPrototipeSpring.Service.AuthorityService;
import com.PrototipoManageService.PetuniaPrototipeSpring.model.Authority;

import java.util.List;

// Anotación que indica que esta clase es un controlador REST
@RestController
// Define la ruta base para todos los endpoints de este controlador
@RequestMapping("/api/ac")
public class AuthorityController {

    // Declaración del servicio que maneja la lógica de negocio
    private final AuthorityService authorityService;

    // Constructor que inyecta el servicio
    public AuthorityController(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    // Endpoint POST para crear una nueva autoridad
    @PostMapping
    public ResponseEntity<Authority> createAuthority(@RequestBody Authority authority) {
        // Llama al servicio para crear la autoridad y la guarda
        Authority saved = authorityService.createAuthority(authority);
        // Retorna una respuesta con estado CREATED y la autoridad guardada
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Endpoint GET para obtener todas las autoridades
    @GetMapping
    public ResponseEntity<List<Authority>> getAuthorities() {
        // Retorna una respuesta OK con la lista de todas las autoridades
        return ResponseEntity.ok(authorityService.getAuthorities());
    }

    // Endpoint DELETE para eliminar una autoridad por su ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthority(@PathVariable Long id) {
        // Llama al servicio para eliminar la autoridad
        authorityService.deleteAuthority(id);
        // Retorna una respuesta sin contenido (204 No Content)
        return ResponseEntity.noContent().build();
    }
}