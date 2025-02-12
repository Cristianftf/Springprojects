package com.PrototipoManageService.PetuniaPrototipeSpring.config;

import java.util.concurrent.TimeUnit;

// Importación necesaria para habilitar clientes Feign
import org.springframework.cloud.openfeign.EnableFeignClients;
// Importación para marcar esta clase como configuración de Spring
import org.springframework.context.annotation.Configuration;
// Importación para configuración de timeouts
import feign.Request;
import org.springframework.context.annotation.Bean;

// Indica que esta clase es una configuración de Spring
@Configuration
// Habilita el uso de clientes Feign en la aplicación
@EnableFeignClients
public class FeignConfig {

    // Configuración de timeouts para las peticiones Feign
    @Bean
    public Request.Options feignOptions() {
        // Configura 5 segundos para el timeout de conexión y 10 segundos para el timeout de lectura
        return new Request.Options(5000, TimeUnit.MILLISECONDS, 10000, TimeUnit.MILLISECONDS, false);
    }
}
