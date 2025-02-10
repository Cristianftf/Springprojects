package cu.xetid.cav.pdfsigner.controllers;

// Importaciones necesarias para el funcionamiento del controlador
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cu.xetid.cav.pdfsigner.validation.Pkcs12ValidationException;
import cu.xetid.cav.pdfsigner.validation.Pkcs12Validator;
import cu.xetid.cav.pdfsigner.validation.cert.CaCertificatesStore;

// Controlador REST para manejar operaciones con certificados PKCS12
@RestController
@RequestMapping("pkcs12")
@CrossOrigin(origins = "*")
public class Pkcs12Cotroller {

  // Endpoint para parsear un archivo PKCS12 y obtener información básica del certificado
  @PostMapping("parse")
  public ResponseEntity<?> parsePkcs12(
    @RequestPart("pkcs12") MultipartFile pkcs12File, // Archivo PKCS12
    @RequestPart("password") String password // Contraseña del archivo
  ) {
    var pkcs12Validator = new Pkcs12Validator();
    try {
      // Parsea el certificado del archivo PKCS12
      X509Certificate cert = pkcs12Validator.parse(
        pkcs12File.getInputStream(),
        password
      );
      // Obtiene el sujeto y emisor del certificado
      X500Principal subject = cert.getSubjectX500Principal();
      X500Principal issuer = cert.getIssuerX500Principal();
      // Retorna la información del certificado
      return ResponseEntity.ok(
        Map.ofEntries(
          Map.entry("notBefore", cert.getNotBefore()),
          Map.entry("notAfter", cert.getNotAfter()),
          Map.entry("subject", subject.getName()),
          Map.entry("issuer", issuer.getName())
        )
      );
    } catch (Pkcs12ValidationException | IOException e) {
      // Manejo de errores específicos
      return ResponseEntity.ok(
        Map.ofEntries(Map.entry("message", e.getMessage()))
      );
    } catch (Exception e) {
      // Manejo de errores generales
      return ResponseEntity.ok(
        Map.ofEntries(Map.entry("message", e.getMessage()))
      );
    }
  }

  // Endpoint para validar un archivo PKCS12 contra certificados de confianza
  @PostMapping("/validate")
  public ResponseEntity<?> validateAndParsePkcs12(
    @RequestPart("pkcs12") MultipartFile pkcs12File, // Archivo PKCS12
    @RequestPart("password") String password, // Contraseña del archivo
    @RequestParam(value = "user_id", required = false) String userId // ID de usuario opcional
  ) {
    var pkcs12Validator = new Pkcs12Validator();
    try {
      // Obtiene los certificados de confianza predeterminados
      Set<X509Certificate> trustedCertificates = CaCertificatesStore
        .getInstance()
        .getDefaultTrustedCaCertificates();
      Set<X509Certificate> customTrustedCertificates = new HashSet<>(trustedCertificates);
      // Si se proporciona un ID de usuario, obtiene certificados de confianza personalizados
      if (userId != null) {
        X509Certificate[] customTrustedCertificatesArray = CaCertificatesStore
          .getInstance()
          .fetchTrustedCertificates("custom-trusted-certificates", userId != null ? userId : "@");
        customTrustedCertificates.addAll(List.of(customTrustedCertificatesArray));
      }
      // Valida el certificado contra los certificados de confianza
      X509Certificate cert = pkcs12Validator.validate(
        pkcs12File.getInputStream(),
        password,
        customTrustedCertificates
      );
      // Obtiene el sujeto y emisor del certificado
      X500Principal subject = cert.getSubjectX500Principal();
      X500Principal issuer = cert.getIssuerX500Principal();
      // Retorna la información del certificado validado
      return ResponseEntity.ok(
        Map.ofEntries(
          Map.entry("isValid", true),
          Map.entry("notBefore", cert.getNotBefore()),
          Map.entry("notAfter", cert.getNotAfter()),
          Map.entry("subject", subject.getName()),
          Map.entry("issuer", issuer.getName())
        )
      );
    } catch (Pkcs12ValidationException | IOException e) {
      // Manejo de errores específicos
      return ResponseEntity.ok(
        Map.ofEntries(Map.entry("isValid", false), Map.entry("message", e.getMessage()))
      );
    } catch (Exception e) {
      // Manejo de errores generales
      return ResponseEntity.ok(
        Map.ofEntries(Map.entry("isValid", false), Map.entry("message", e.getMessage()))
      );
    }
  }

  // Endpoint para cambiar la contraseña de un archivo PKCS12
  @PostMapping("/change-password")
  public ResponseEntity<Object> signDocument(
    @RequestPart("pkcs12") MultipartFile pkcs12File, // Archivo PKCS12
    @RequestPart("oldPassword") String oldPassword, // Contraseña actual
    @RequestPart("newPassword") String newPassword, // Nueva contraseña
    HttpServletResponse response
  ) {
    var pkcs12Validator = new Pkcs12Validator();
    try {
      // Crea un stream para almacenar el nuevo archivo PKCS12
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      // Cambia la contraseña del archivo PKCS12
      pkcs12Validator.changePassword(pkcs12File.getInputStream(), oldPassword, newPassword, out);
      long contentLength = out.size();

      // Configura la respuesta HTTP para descargar el nuevo archivo
      response.setStatus(200);
      response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=new.p12");
      response.setContentLengthLong(contentLength);
      response.setContentType(MediaType.TEXT_PLAIN.toString());
      response.getOutputStream().write(out.toByteArray());
      return null;
    } catch (Exception e) {
      // Manejo de errores
      e.printStackTrace();
      return ResponseEntity.badRequest().body(e);
    }
  } 
}