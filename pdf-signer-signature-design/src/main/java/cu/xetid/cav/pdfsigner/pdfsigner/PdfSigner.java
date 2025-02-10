package cu.xetid.cav.pdfsigner.pdfsigner;

// Importaciones necesarias para el funcionamiento de la clase
import cu.xetid.cav.pdfsigner.dto.OptionsDto;
import cu.xetid.cav.pdfsigner.pdfsigner.signature.Signature;
import cu.xetid.cav.pdfsigner.util.RSA;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.lang.Nullable;

// Anotaciones para habilitar el logging y generar constructor con todos los argumentos
@Slf4j
@AllArgsConstructor
public class PdfSigner {

  // URL del servidor de sellado de tiempo
  @NonNull
  private final String tsaUrl;

  // Objeto que contiene las opciones de configuración
  @NonNull
  private final OptionsDto optionsDto;

  // Stream de entrada del archivo PDF a firmar
  @NonNull
  private final InputStream pdfInputStream;

  // Stream de entrada del certificado PKCS12
  @NonNull
  private final InputStream pkcs12InputStream;

  // Stream de entrada de la imagen de la firma (opcional)
  @Nullable
  private final InputStream signatureImageInputStream;

  public void sign(OutputStream outputStream)
    throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException, TranscoderException, Exception {
    // Obtiene el almacén de claves
    log.debug("obteniendo el almacén de claves");
    KeyStore keyStore = KeyStore.getInstance("PKCS12");
    // Desencripta la contraseña del certificado
    String decryptedPassword = RSA.getInstance().decryptMessage(optionsDto.pkcs12FilePassword);
    char[] keyStorePassword = decryptedPassword.toCharArray();
    // Carga el certificado PKCS12 en el almacén de claves
    log.debug("cargando el PKCS12 en el almacén de claves");
    keyStore.load(this.pkcs12InputStream, keyStorePassword);

    // Crea el documento PDF
    log.debug("creando el documento PDF");
    PDDocument document = PDDocument.load(this.pdfInputStream);

    // Crea el objeto de firma
    log.debug("creando la firma");
    Signature signature = new Signature(keyStore, keyStorePassword, tsaUrl, optionsDto);
    // Establece la entidad firmante
    signature.setSignerEntity(optionsDto.signerEntity);
    // Establece el cargo del firmante
    signature.setSignerJobTitle(optionsDto.signerJobTitle);
    // Si existe una imagen de firma, la establece
    if (this.signatureImageInputStream != null) {
      signature.setSignatureImage(this.signatureImageInputStream);
    }
    // Firma el documento
    log.debug("firmando el documento");
    signature.signDetached(document, outputStream);
  }
}
