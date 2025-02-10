import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.openfeign.EnableFeignClients;

@Configuration
@EnableFeignClients
public class FeignConfig {

    // Aquí podrías agregar configuraciones adicionales si las necesitas, como el timeout.
}
