// Paquete donde se encuentra la clase
package com.PrototipoManageService.PetuniaPrototipeSpring.Service;

// Importaciones necesarias para el funcionamiento del servicio
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.PrototipoManageService.PetuniaPrototipeSpring.model.DigitalIdentity;
import com.PrototipoManageService.PetuniaPrototipeSpring.repository.DigitalIdentityRepository;
import com.PrototipoManageService.PetuniaPrototipeSpring.util.EncryptionUtil;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Anotación que indica que esta clase es un servicio de Spring
@Service
public class DigitalIdentityService {

    // Repositorio para acceder a los datos de identidad digital
    private final DigitalIdentityRepository repository;
    // Utilidad para encriptar y desencriptar contraseñas
    private final EncryptionUtil encryptionUtil;

    // Constructor que inyecta las dependencias necesarias
    public DigitalIdentityService(DigitalIdentityRepository repository, EncryptionUtil encryptionUtil) {
        this.repository = repository;
        this.encryptionUtil = encryptionUtil;
    }

    // Método para crear una nueva identidad digital con contraseña encriptada
    @Transactional
    public DigitalIdentity createDigitalIdentity(DigitalIdentity identity, String plainPassword) {
        // Encripta la contraseña proporcionada
        String encrypted = encryptionUtil.encrypt(plainPassword);
        // Establece la contraseña encriptada en la identidad
        identity.setEncryptedPassword(encrypted);
        // Guarda la identidad en la base de datos
        return repository.save(identity);
    }

    // Método para obtener todas las identidades de un usuario específico
    public List<DigitalIdentity> getIdentitiesByUser(String userReferenceId) {
        return repository.findByUserReferenceId(userReferenceId);
    }

    // Método para obtener una identidad específica por su ID
    public Optional<DigitalIdentity> getIdentityById(UUID id) {
        return repository.findById(id);
    }

    // Método para eliminar una identidad digital por su ID
    @Transactional
    public void deleteDigitalIdentity(UUID id) {
        repository.deleteById(id);
    }

    // Método para recuperar el archivo PKCS12 verificando la contraseña
    @Transactional(readOnly = true)
    public byte[] retrievePkcs12File(UUID id, String providedPassword) {
        // Busca la identidad por ID
        Optional<DigitalIdentity> optionalIdentity = repository.findById(id);
        if (optionalIdentity.isPresent()) {
            DigitalIdentity identity = optionalIdentity.get();
            // Desencripta la contraseña almacenada
            String storedPassword = encryptionUtil.decrypt(identity.getEncryptedPassword());
            // Verifica si la contraseña proporcionada coincide
            if (storedPassword.equals(providedPassword)) {
                // Retorna el archivo PKCS12 si la contraseña es correcta
                return identity.getPkcs12File();
            }
        }
        // Retorna null si no se encuentra la identidad o la contraseña es incorrecta
        return null;
    }

    // Método para actualizar la contraseña de una identidad digital
    @Transactional
    public DigitalIdentity updateDigitalIdentityPassword(UUID id, String oldPlainPassword, String newPlainPassword) {
        // Busca la identidad por ID
        Optional<DigitalIdentity> optionalIdentity = repository.findById(id);
        if (optionalIdentity.isPresent()) {
            DigitalIdentity identity = optionalIdentity.get();
            // Desencripta la contraseña almacenada
            String storedPassword = encryptionUtil.decrypt(identity.getEncryptedPassword());
            // Verifica si la contraseña antigua coincide
            if (!storedPassword.equals(oldPlainPassword)) {
                throw new RuntimeException("La contraseña antigua no coincide.");
            }
            // Encripta la nueva contraseña
            String encryptedNew = encryptionUtil.encrypt(newPlainPassword);
            // Establece la nueva contraseña encriptada
            identity.setEncryptedPassword(encryptedNew);
            // Guarda los cambios en la base de datos
            return repository.save(identity);
        } else {
            throw new RuntimeException("Identidad digital no encontrada");
        }
    }
}