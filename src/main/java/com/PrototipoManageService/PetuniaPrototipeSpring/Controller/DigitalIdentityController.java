// Declaración del paquete donde se encuentra la clase
package com.PrototipoManageService.PetuniaPrototipeSpring.Controller;

// Importación de clases necesarias de Spring y otras dependencias
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.PrototipoManageService.PetuniaPrototipeSpring.Service.DigitalIdentityService;
import com.PrototipoManageService.PetuniaPrototipeSpring.dto.ChangePasswordRequest;
import com.PrototipoManageService.PetuniaPrototipeSpring.dto.GetPkcs12Request;
import com.PrototipoManageService.PetuniaPrototipeSpring.model.DigitalIdentity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Anotación que indica que esta clase es un controlador REST
@RestController
// Define la ruta base para todos los endpoints de este controlador
@RequestMapping("/api/digitalid")
public class DigitalIdentityController {

    // Inyección del servicio que maneja la lógica de negocio
    private final DigitalIdentityService service;

    // Constructor que inicializa el servicio
    public DigitalIdentityController(DigitalIdentityService service) {
        this.service = service;
    }

    // Endpoint para crear una nueva identidad digital
    @PostMapping
    public ResponseEntity<DigitalIdentity> createIdentity(@RequestBody CreateDigitalIdentityRequest request) {
        // Crea una nueva instancia de DigitalIdentity
        DigitalIdentity identity = new DigitalIdentity();
        // Establece el ID de referencia del usuario
        identity.setUserReferenceId(request.getUserReferenceId());
        // Establece la fecha de expiración
        identity.setExpirationDate(request.getExpirationDate());
        // Establece el archivo PKCS12
        identity.setPkcs12File(request.getPkcs12File());
        // Guarda la identidad digital en el sistema
        DigitalIdentity savedIdentity = service.createDigitalIdentity(identity, request.getPlainPassword());
        // Retorna la identidad guardada con estado HTTP 201 (CREATED)
        return new ResponseEntity<>(savedIdentity, HttpStatus.CREATED);
    }

    // Endpoint para obtener todas las identidades de un usuario
    @GetMapping
    public ResponseEntity<List<DigitalIdentity>> getIdentities(@RequestParam String userReferenceId) {
        // Retorna la lista de identidades asociadas al usuario
        return ResponseEntity.ok(service.getIdentitiesByUser(userReferenceId));
    }

    // Endpoint para obtener una identidad específica por su ID
    @GetMapping("/{id}")
    public ResponseEntity<DigitalIdentity> getIdentityById(@PathVariable UUID id) {
        // Busca la identidad por ID
        Optional<DigitalIdentity> identity = service.getIdentityById(id);
        // Retorna la identidad si existe, o 404 si no se encuentra
        return identity.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Endpoint para eliminar una identidad digital
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIdentity(@PathVariable UUID id) {
        // Elimina la identidad digital
        service.deleteDigitalIdentity(id);
        // Retorna 204 (NO_CONTENT) indicando éxito sin contenido
        return ResponseEntity.noContent().build();
    }

    // Endpoint para recuperar el archivo PKCS12 de una identidad digital
    @PostMapping("/{id}/pkcs12")
    public ResponseEntity<byte[]> getPkcs12File(@PathVariable UUID id, @RequestBody GetPkcs12Request request) {
        // Obtiene el archivo PKCS12
        byte[] file = service.retrievePkcs12File(id, request.getDigitalidPassword());
        // Si el archivo es null, retorna 401 (UNAUTHORIZED)
        if (file == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Retorna el archivo si la operación fue exitosa
        return ResponseEntity.ok(file);
    }

    // Endpoint para cambiar la contraseña de una identidad digital
    @PutMapping("/{id}/password")
    public ResponseEntity<DigitalIdentity> changePassword(@PathVariable UUID id,
                                                            @RequestBody ChangePasswordRequest request) {
        try {
            // Intenta actualizar la contraseña
            DigitalIdentity updatedIdentity = service.updateDigitalIdentityPassword(id, request.getOldPassword(), request.getNewPassword());
            // Retorna la identidad actualizada si la operación fue exitosa
            return ResponseEntity.ok(updatedIdentity);
        } catch (RuntimeException ex) {
            // Retorna 400 (BAD_REQUEST) si hay algún error
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}