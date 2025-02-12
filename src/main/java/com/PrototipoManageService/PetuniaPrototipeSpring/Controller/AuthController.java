// Declaración del paquete donde se encuentra la clase
package com.PrototipoManageService.PetuniaPrototipeSpring.Controller;

// Importación de clases necesarias de Spring y componentes personalizados
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.PrototipoManageService.PetuniaPrototipeSpring.Service.UserService;
import com.PrototipoManageService.PetuniaPrototipeSpring.security.JwtUtil;

// Anotación que indica que esta clase es un controlador REST
@RestController
// Define la ruta base para todos los endpoints en este controlador
@RequestMapping("/api/auth")
public class AuthController {

    // Inyección de dependencias del servicio de usuarios
    private final UserService userService;
    // Inyección de dependencias de la utilidad JWT
    private final JwtUtil jwtUtil;

    // Constructor que inicializa las dependencias
    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    // Endpoint para registrar nuevos usuarios
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestParam String username, @RequestParam String password) {
        // Llama al servicio para registrar un nuevo usuario
        userService.registerUser(username, password);
        // Retorna una respuesta exitosa
        return ResponseEntity.ok("User registered successfully!");
    }

    // Endpoint para el inicio de sesión de usuarios
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username, @RequestParam String password) {
        // En una implementación real se verificaría la contraseña y se emitiría el token si la validación es correcta.
        // Genera un token JWT para el usuario
        String token = jwtUtil.generateToken(username);
        // Retorna el token generado
        return ResponseEntity.ok(token);
    }
}