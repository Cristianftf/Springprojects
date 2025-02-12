package com.PrototipoManageService.PetuniaPrototipeSpring.security;

// Importaciones necesarias para el funcionamiento del filtro
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.PrototipoManageService.PetuniaPrototipeSpring.Service.UserService;
import com.PrototipoManageService.PetuniaPrototipeSpring.model.User;

// Clase que implementa un filtro de autenticación JWT extendiendo OncePerRequestFilter
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Inyección de dependencias necesarias
    private final JwtUtil jwtUtil;
    private final UserService userService;

    // Constructor para inicializar las dependencias
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
                                    throws ServletException, IOException {

        // Obtiene el header de autorización de la petición
        String header = request.getHeader("Authorization");
        String token = null;
        String username = null;

        // Verifica si el header existe y comienza con "Bearer "
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            // Extrae el token eliminando el prefijo "Bearer "
            token = header.substring(7);
            try {
                // Extrae el username del token
                username = jwtUtil.extractUsername(token);
            } catch (Exception e) {
                // Registra cualquier error durante la extracción del username
                logger.error("Error al extraer el username del token", e);
            }
        }

        // Verifica si se extrajo un username y si no hay autenticación previa
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Busca el usuario en la base de datos
            var userOptional = userService.findByUsername(username);
            // Valida el token y la existencia del usuario
            if (userOptional.isPresent() && jwtUtil.validateToken(token, username)) {
                User user = userOptional.get();
                // Crea un token de autenticación con los detalles del usuario
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());
                // Establece los detalles de la autenticación
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // Establece la autenticación en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // Continúa con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}