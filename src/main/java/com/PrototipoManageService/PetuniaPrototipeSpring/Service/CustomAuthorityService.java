// Declaración del paquete donde se encuentra la clase
package com.PrototipoManageService.PetuniaPrototipeSpring.Service;

// Importación de las clases necesarias
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.PrototipoManageService.PetuniaPrototipeSpring.model.Authority;
import com.PrototipoManageService.PetuniaPrototipeSpring.repository.AuthorityRepository;
import java.util.List;

// Anotación que indica que esta clase es un servicio de Spring
@Service
public class CustomAuthorityService {

    // Declaración del repositorio que se utilizará para acceder a los datos
    private final AuthorityRepository authorityRepository;

    // Constructor que inyecta el repositorio
    public CustomAuthorityService(AuthorityRepository authorityRepository) {
        this.authorityRepository = authorityRepository;
    }

    // Método para crear una nueva autoridad, con manejo de transacciones
    @Transactional
    public Authority createCustomAuthority(Authority authority) {
        return authorityRepository.save(authority);
    }

    // Método para obtener la lista de autoridades de un usuario específico
    public List<Authority> getCustomAuthorities(String userReferenceId) {
        return authorityRepository.findByUserReferenceId(userReferenceId);
    }

    // Método para eliminar una autoridad por su ID, con manejo de transacciones
    @Transactional
    public void deleteCustomAuthority(Long id) {
        authorityRepository.deleteById(id);
    }
}