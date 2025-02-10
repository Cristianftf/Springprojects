package cu.xetid.cav.pdfsigner.controllers;

// Importaciones necesarias para el funcionamiento del controlador
import java.io.File;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cu.xetid.cav.pdfsigner.dto.OptionsDto;
import cu.xetid.cav.pdfsigner.pdfsigner.PdfSigner;
import cu.xetid.cav.pdfsigner.validation.PdfValidator;
import cu.xetid.cav.pdfsigner.validation.cert.CaCertificatesStore;
import lombok.extern.slf4j.Slf4j;

// Anotaciones para definir el controlador y sus características
@Slf4j // Habilita el logging
@RestController // Define que es un controlador REST
@RequestMapping("pdf") // Define la ruta base del controlador
@CrossOrigin(origins = "*") // Permite peticiones desde cualquier origen
public class PdfController {
  // URL del servicio de sellado de tiempo
  @Value("${pdfsigner.tsa-url}")
  private String tsaUrl;

  // Endpoint para firmar documentos PDF
  @PostMapping(value = "sign", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public ResponseEntity<Object> signDocument(
    @RequestPart("pdf") MultipartFile pdfFile, // Archivo PDF a firmar
    @RequestPart("pkcs12") MultipartFile pkcs12File, // Certificado PKCS12
    @RequestPart(
      value = "signatureImage",
      required = false
    ) @Nullable MultipartFile signatureImageFile, // Imagen de firma opcional
    @RequestPart("options") @Valid OptionsDto optionsDto, // Opciones de firma
    HttpServletResponse response
  ) {
    try {
      // Crear instancia del firmador PDF con los parámetros necesarios
      var pdfSigner = new PdfSigner(
        tsaUrl,
        optionsDto,
        pdfFile.getInputStream(),
        pkcs12File.getInputStream(),
        signatureImageFile != null ? signatureImageFile.getInputStream() : null
      );
      // Buffer para almacenar el PDF firmado
      var out = new FastByteArrayOutputStream();
      log.debug("signing pdf");
      pdfSigner.sign(out); // Realizar la firma
      log.debug("signed pdf");
      long contentLength = out.size();

      // Configurar la respuesta HTTP
      response.setStatus(200);
      response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=signed_file.pdf");
      response.setContentLengthLong(contentLength);
      response.setContentType(MediaType.APPLICATION_PDF.toString());
      response.getOutputStream().write(out.toByteArray());
      return null;
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.badRequest().body(e);
    }
  }

  // Endpoint para validar documentos PDF firmados
  @PostMapping(value = "validate", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public ResponseEntity<?> validateDocument(
    @RequestPart("pdf") MultipartFile pdfFile, // Archivo PDF a validar
    @RequestParam(value = "user_id", required = false) String userId // ID de usuario opcional
  ) throws IOException {
    // Crear archivo temporal para el PDF
    File tempFile = File.createTempFile("validate-pdf", null);
    pdfFile.transferTo(tempFile);

    try {
      // Crear instancia del validador
      var pdfValidator = new PdfValidator();
      // Obtener certificados raíz confiables por defecto
      Set<X509Certificate> trustedCertificates = CaCertificatesStore
        .getInstance()
        .getDefaultTrustedCaCertificates();
      Set<X509Certificate> customTrustedCertificates = new HashSet<>(trustedCertificates);
      // Si se proporciona un ID de usuario, agregar certificados personalizados
      if (userId != null) {
        X509Certificate[] customTrustedCertificatesArray = CaCertificatesStore
          .getInstance()
          .fetchTrustedCertificates("custom-trusted-certificates", userId != null ? userId : "@");
        customTrustedCertificates.addAll(List.of(customTrustedCertificatesArray));
      }
      // Realizar la validación
      var result = pdfValidator.validate(tempFile, customTrustedCertificates);
      tempFile.delete(); // Eliminar archivo temporal
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.error(e.getMessage());
      tempFile.delete(); // Eliminar archivo temporal en caso de error
      return ResponseEntity.status(500).body(Map.entry("message", e.getMessage()));
    }
  }
}