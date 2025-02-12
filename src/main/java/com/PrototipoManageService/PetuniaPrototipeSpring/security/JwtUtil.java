// Paquete donde se encuentra la clase
package com.PrototipoManageService.PetuniaPrototipeSpring.security;

// Importaciones necesarias para el funcionamiento
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.function.Function;
import javax.crypto.SecretKey;

// Anotación que marca esta clase como un componente de Spring
@Component
public class JwtUtil {
    // Crea una clave secreta usando HMAC-SHA con una cadena predefinida
    private final SecretKey key = Keys.hmacShaKeyFor("supersecretkey".getBytes());

    // Método para generar un nuevo token JWT
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)                // Establece el usuario como subject del token
                .issuedAt(new Date())            // Establece la fecha de emisión
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))  // Establece la fecha de expiración (10 horas)
                .signWith(key)                    // Firma el token con la clave secreta
                .compact();                       // Construye el token final
    }

    // Método para extraer el nombre de usuario del token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Método para validar si un token es válido para un usuario específico
    public boolean validateToken(String token, String username) {
        return extractUsername(token).equals(username) && !isTokenExpired(token);
    }

    // Método privado para verificar si el token ha expirado
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    // Método genérico para extraer claims específicos del token
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parser()            // Crea un parser JWT
                .verifyWith(key)                 // Verifica el token con la clave secreta
                .build()                         // Construye el parser
                .parseSignedClaims(token)        // Parsea el token firmado
                .getPayload();                   // Obtiene el payload del token
        return claimsResolver.apply(claims);     // Aplica la función resolver al claim
    }
}