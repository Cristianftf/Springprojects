// Declaración del paquete que contiene la clase
package cu.xetid.cav.pdfsigner.config;

// Importación de clases necesarias
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// Anotación que marca esta clase como un componente de Spring
@Component
public class AppSecurityProviderConfig {

  // Método que se ejecuta cuando la aplicación está lista, anotado como un EventListener
  @EventListener(ApplicationReadyEvent.class)
  public void addBCSecurityProviderOnAppStart() {
    // Agrega el proveedor de seguridad BouncyCastle al sistema
    Security.addProvider(new BouncyCastleProvider());
  }
}