// Declaración del paquete donde se encuentra la clase
package com.PrototipoManageService.PetuniaPrototipeSpring.Service;

// Importaciones necesarias para el funcionamiento de la clase
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.PrototipoManageService.PetuniaPrototipeSpring.model.User;
import com.PrototipoManageService.PetuniaPrototipeSpring.repository.UserRepository;
import java.util.Optional;

// Anotación que indica que esta clase es un servicio de Spring
@Service
public class UserService {

    // Repositorio para acceder a los datos de usuarios
    private final UserRepository userRepository;
    // Codificador de contraseñas para encriptar las contraseñas de usuarios
    private final PasswordEncoder passwordEncoder;  // BCryptPasswordEncoder inyectado en SecurityConfig

    // Constructor que inyecta las dependencias necesarias
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Método para registrar un nuevo usuario
    public User registerUser(String username, String password) {
        // Crea una nueva instancia de Usuario
        User user = new User();
        // Establece el nombre de usuario
        user.setUsername(username);
        // Encripta la contraseña y la establece
        user.setPassword(passwordEncoder.encode(password));
        // Guarda el usuario en la base de datos y lo retorna
        return userRepository.save(user);
    }

    // Método para buscar un usuario por su nombre de usuario
    public Optional<User> findByUsername(String username) {
        // Busca y retorna el usuario si existe
        return userRepository.findByUsername(username);
    }
}