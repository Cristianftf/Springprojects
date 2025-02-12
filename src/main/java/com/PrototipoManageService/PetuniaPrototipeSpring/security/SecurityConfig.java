// Paquete donde se encuentra la clase de configuración de seguridad
package com.PrototipoManageService.PetuniaPrototipeSpring.security;

// Importaciones necesarias para la configuración de seguridad
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.PrototipoManageService.PetuniaPrototipeSpring.Service.UserService;

// Anotación que indica que esta clase es de configuración
@Configuration
public class SecurityConfig {

    // Inyección de dependencias para el manejo de JWT y servicio de usuarios
    private final JwtUtil jwtUtil;
    private final UserService userService;

    // Constructor para inicializar las dependencias
    public SecurityConfig(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    // Bean que configura la cadena de filtros de seguridad
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilita la protección CSRF
            .csrf(csrf -> csrf.disable())
            // Configura la política de sesión como STATELESS (sin estado)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Configura las reglas de autorización para las peticiones HTTP
            .authorizeHttpRequests(auth -> auth
                // Permite todas las peticiones que coincidan con "/api/auth/**"
                .requestMatchers("/api/auth/**").permitAll()
                // Requiere autenticación para cualquier otra petición
                .anyRequest().authenticated()
            )
            // Añade el filtro JWT antes del filtro de autenticación por usuario y contraseña
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Bean que crea el filtro de autenticación JWT
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil, userService);
    }

    // Bean que configura el codificador de contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}