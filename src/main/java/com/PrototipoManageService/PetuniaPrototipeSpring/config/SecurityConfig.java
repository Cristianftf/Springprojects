// Paquete donde se encuentra la clase
package com.PrototipoManageService.PetuniaPrototipeSpring.config;

// Importaciones necesarias para la configuración de seguridad
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.PrototipoManageService.PetuniaPrototipeSpring.security.JwtAuthenticationFilter;

// Anotación que indica que esta clase es de configuración
@Configuration
public class SecurityConfig {

    // Filtro JWT para autenticación
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Constructor que inyecta el filtro JWT
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // Bean que configura la cadena de filtros de seguridad
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilita la protección CSRF
            .csrf(csrf -> csrf.disable())
            // Configura la gestión de sesiones como STATELESS (sin estado)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Configura las reglas de autorización para las peticiones HTTP
            .authorizeHttpRequests(auth -> auth
                // Permite todas las peticiones a /api/auth/** sin autenticación
                .requestMatchers("/api/auth/**").permitAll()
                // Requiere autenticación para todas las demás peticiones
                .anyRequest().authenticated()
            )
            // Añade el filtro JWT antes del filtro de autenticación por usuario y contraseña
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Bean que proporciona el codificador de contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Utiliza BCrypt para el hash de contraseñas
        return new BCryptPasswordEncoder();
    }
}