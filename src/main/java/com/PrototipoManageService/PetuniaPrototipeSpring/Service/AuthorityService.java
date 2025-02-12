// Declaración del paquete donde se encuentra la clase
package com.PrototipoManageService.PetuniaPrototipeSpring.Service;

// Importación de clases necesarias
import java.util.List;
import org.springframework.stereotype.Service;
import com.PrototipoManageService.PetuniaPrototipeSpring.model.Authority;
import com.PrototipoManageService.PetuniaPrototipeSpring.repository.AuthorityRepository;
import org.springframework.transaction.annotation.Transactional;

// Anotación que indica que esta clase es un servicio de Spring
@Service
public class AuthorityService {

    // Declaración del repositorio que se utilizará para acceder a los datos
    private final AuthorityRepository authorityRepository;

    // Constructor que inyecta el repositorio
    public AuthorityService(AuthorityRepository authorityRepository) {
        this.authorityRepository = authorityRepository;
    }

    // Método para crear una nueva autoridad, anotado como transaccional
    @Transactional
    public Authority createAuthority(Authority authority) {
        // Aquí se podrían extraer metadatos del certificado
        // Guarda la autoridad en la base de datos y retorna la entidad guardada
        return authorityRepository.save(authority);
    }

    // Método para obtener todas las autoridades
    public List<Authority> getAuthorities() {
        // Retorna una lista con todas las autoridades encontradas
        return authorityRepository.findAll();
    }

    // Método para eliminar una autoridad por su ID, anotado como transaccional
    @Transactional
    public void deleteAuthority(Long id) {
        // Elimina la autoridad de la base de datos usando su ID
        authorityRepository.deleteById(id);
    }
}