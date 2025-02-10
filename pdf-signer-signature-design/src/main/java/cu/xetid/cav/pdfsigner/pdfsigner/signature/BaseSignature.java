package cu.xetid.cav.pdfsigner.pdfsigner.signature;

// Importaciones necesarias para el funcionamiento de la clase
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

// Anotación para habilitar el logging
@Slf4j
public abstract class BaseSignature implements SignatureInterface {

  // URL del servidor de sellado de tiempo
  private final String tsaUrl;
  // Clave privada para la firma
  private PrivateKey privateKey;
  // Cadena de certificados
  private Certificate[] certificateChain;

  /**
   * Inicializa el creador de firmas con un almacén de claves (pkcs12) y un PIN que debe
   * usarse para la firma.
   *
   * @param keystore es un almacén de claves pkcs12
   * @param pin      es el PIN para el almacén de claves / clave privada
   * @throws KeyStoreException               si el almacén de claves no ha sido
   *                                         inicializado (cargado)
   * @throws NoSuchAlgorithmException        si no se encuentra el algoritmo para
   *                                         recuperar la clave
   * @throws UnrecoverableKeyException       si la contraseña proporcionada es incorrecta
   * @throws CertificateException            si el certificado no es válido en el
   *                                         momento de la firma
   * @throws IOException                     si no se puede encontrar el certificado
   * @throws CertificateParsingException
   * @throws CertificateNotYetValidException
   * @throws CertificateExpiredException
   */
  public BaseSignature(KeyStore keyStore, char[] password, String tsaUrl)
    throws KeyStoreException, IOException, CertificateParsingException, CertificateExpiredException, CertificateNotYetValidException, UnrecoverableKeyException, NoSuchAlgorithmException {
    this.tsaUrl = tsaUrl;
    // Obtiene el primer alias del almacén de claves y obtiene la clave privada.
    // Se podría usar un método o constructor alternativo para establecer un alias específico.
    Enumeration<String> aliases = keyStore.aliases();
    String alias;
    Certificate cert = null;
    while (cert == null && aliases.hasMoreElements()) {
      // Obtiene el siguiente alias
      alias = aliases.nextElement();
      // Establece la clave privada
      setPrivateKey((PrivateKey) keyStore.getKey(alias, password));
      // Obtiene la cadena de certificados
      Certificate[] certChain = keyStore.getCertificateChain(alias);
      if (certChain != null) {
        setCertificateChain(certChain);
        cert = certChain[0];
        if (cert instanceof X509Certificate) {
          // Evita certificados expirados
          ((X509Certificate) cert).checkValidity();
          // Verifica el uso del certificado
          var errors = SigUtils.checkCertificateUsage((X509Certificate) cert);
          if (!errors.isEmpty()) {
            throw new IOException(
              errors
                .stream()
                .reduce(
                  "",
                  (acc, curr) ->
                    acc.trim().endsWith(".") ? acc.trim() + " " + curr : acc.trim() + ". " + curr
                )
            );
          }
        }
      }
    }

    // Si no se encuentra certificado, lanza excepción
    if (cert == null) {
      throw new IOException("No se pudo encontrar el certificado");
    }
  }

  // Establece la clave privada
  public void setPrivateKey(PrivateKey privateKey) {
    this.privateKey = privateKey;
  }

  // Establece la cadena de certificados
  public void setCertificateChain(Certificate[] certificateChain) {
    this.certificateChain = certificateChain;
  }

  // Obtiene la cadena de certificados
  public Certificate[] getCertificateChain() {
    return certificateChain;
  }

  /**
   * Implementación de muestra de SignatureInterface.
   * 
   * Este método será llamado desde dentro de pdfbox y creará la firma PKCS #7.
   * El InputStream proporcionado contiene los bytes dados por el rango de bytes.
   * 
   * Este método es solo para uso interno.
   * 
   * Use su biblioteca criptográfica favorita para implementar la creación de firmas PKCS #7.
   * Si desea crear el hash y la firma por separado (por ejemplo, para transferir solo
   * el hash a una aplicación externa), lea las respuestas enlazadas.
   *
   * @param content es el contenido como un (Filter)InputStream
   * @return firma como un array de bytes
   * @throws IOException si algo salió mal
   */
  @Override
  public byte[] sign(InputStream content) throws IOException {
    try {
      // Crea el generador de datos firmados CMS
      CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
      X509Certificate cert = (X509Certificate) certificateChain[0];
      // Crea el firmante de contenido con SHA256WithRSA
      ContentSigner sha1Signer = new JcaContentSignerBuilder("SHA256WithRSA").build(privateKey);
      // Agrega la información del firmante
      gen.addSignerInfoGenerator(
        new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().build())
          .build(sha1Signer, cert)
      );
      // Agrega los certificados
      gen.addCertificates(new JcaCertStore(Arrays.asList(certificateChain)));
      CMSProcessableInputStream msg = new CMSProcessableInputStream(content);
      // Genera los datos firmados
      CMSSignedData signedData = gen.generate(msg, false);

      try {
        // Agrega el sello de tiempo a la firma
        signedData = this.addSignedTimestamp(signedData);
      } catch (Exception e) {
        log.error("Error al agregar el sello de tiempo firmado: " + e.getMessage());
        throw e;
      }

      log.debug("obteniendo codificación");
      // Codifica los datos firmados
      byte[] encoded = signedData.getEncoded();
      log.debug("codificado");
      return encoded;
    } catch (GeneralSecurityException | CMSException | OperatorCreationException e) {
      log.error("Error en la firma: " + e);
      throw new IOException(e);
    }
  }

  // Agrega el sello de tiempo a los datos firmados
  private CMSSignedData addSignedTimestamp(CMSSignedData signedData)
    throws IOException, NoSuchAlgorithmException {
    ValidationTimeStamp validation = new ValidationTimeStamp(tsaUrl);
    log.debug("agregando sello de tiempo firmado");
    CMSSignedData cMSSignedData = validation.addSignedTimeStamp(signedData);
    log.debug("sello de tiempo firmado agregado");
    return cMSSignedData;
  }
}
