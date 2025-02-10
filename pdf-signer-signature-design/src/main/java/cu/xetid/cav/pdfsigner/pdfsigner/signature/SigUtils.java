package cu.xetid.cav.pdfsigner.pdfsigner.signature;

// Importaciones necesarias para el funcionamiento de la clase
import cu.xetid.cav.pdfsigner.validation.cert.CertificateVerificationException;
import cu.xetid.cav.pdfsigner.validation.cert.CertificateVerifier;
import cu.xetid.cav.pdfsigner.validation.cert.RevokedCertificateException;
import cu.xetid.cav.pdfsigner.validation.util.ConnectedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.encryption.SecurityProvider;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.util.Selector;
import org.bouncycastle.util.Store;

// Anotación para habilitar el logging
@Slf4j
public class SigUtils {

  /**
   * Registra si el certificado no es válido para el uso de firma. 
   * Hacerlo de todos modos resulta en que Adobe Reader falle al validar el PDF.
   *
   * @param x509Certificate
   * @throws java.security.cert.CertificateParsingException
   */
  public static List<String> checkCertificateUsage(X509Certificate x509Certificate)
    throws CertificateParsingException {
    // Lista para almacenar advertencias
    var warnings = new LinkedList<String>();

    // Verifica si el certificado del firmante es "válido para su uso"
    boolean[] keyUsage = x509Certificate.getKeyUsage();
    if (keyUsage != null && !keyUsage[0] && !keyUsage[1]) {
      warnings.add("El certificado no tiene contemplado en su uso la firma de documentos");
    }
    // Verifica los usos extendidos del certificado
    List<String> extendedKeyUsage = x509Certificate.getExtendedKeyUsage();
    if (
      extendedKeyUsage != null &&
      !extendedKeyUsage.contains(KeyPurposeId.id_kp_emailProtection.toString()) &&
      !extendedKeyUsage.contains(KeyPurposeId.id_kp_codeSigning.toString()) &&
      !extendedKeyUsage.contains(KeyPurposeId.anyExtendedKeyUsage.toString()) &&
      !extendedKeyUsage.contains("1.2.840.113583.1.1.5") &&
      !extendedKeyUsage.contains("1.3.6.1.4.1.311.10.3.12")
    ) {
      warnings.add(
        "El certificado no tiene contemplado en su uso extendido la firma de documentos"
      );
    }

    return warnings;
  }

  // Extrae el token de sello de tiempo de la información del firmante
  public static TimeStampToken extractTimeStampTokenFromSignerInformation(
    SignerInformation signerInformation
  ) throws IOException, CMSException, TSPException {
    if (signerInformation.getUnsignedAttributes() == null) {
      return null;
    }
    // Obtiene los atributos no firmados
    AttributeTable unsignedAttributes = signerInformation.getUnsignedAttributes();
    Attribute attribute = unsignedAttributes.get(
      PKCSObjectIdentifiers.id_aa_signatureTimeStampToken
    );
    if (attribute == null) {
      return null;
    }
    // Procesa el objeto ASN1 y crea el token de sello de tiempo
    ASN1Object obj = (ASN1Object) attribute.getAttrValues().getObjectAt(0);
    CMSSignedData signedTSTData = new CMSSignedData(obj.getEncoded());
    return new TimeStampToken(signedTSTData);
  }

  // Valida el token de sello de tiempo
  public static void validateTimestampToken(TimeStampToken timeStampToken)
    throws IOException, CertificateException, TSPException, OperatorCreationException {
    @SuppressWarnings("unchecked")
    Collection<X509CertificateHolder> tstMatches = timeStampToken
      .getCertificates()
      .getMatches((Selector<X509CertificateHolder>) timeStampToken.getSID());
    X509CertificateHolder certificateHolder = tstMatches.iterator().next();
    SignerInformationVerifier siv = new JcaSimpleSignerInfoVerifierBuilder()
      .setProvider(SecurityProvider.getProvider())
      .build(certificateHolder);
    timeStampToken.validate(siv);
  }

  /**
   * Verifica la cadena de certificados hasta la raíz, incluyendo OCSP o CRL.
   * Sin embargo, esto no prueba si el certificado raíz está en una lista de confianza.
   *
   * @param certificatesStore Almacén de certificados
   * @param certFromSignedData Certificado de los datos firmados
   * @param signDate Fecha de firma
   */
  public static void verifyCertificateChain(
    Store<X509CertificateHolder> certificatesStore,
    X509Certificate certFromSignedData,
    Set<X509Certificate> trustedCertificates,
    Date signDate
  )
    throws GeneralSecurityException, CertificateVerificationException, RevokedCertificateException, IOException {
    // Obtiene la cadena de certificados intermedios
    Set<X509Certificate> additionalCerts = getCertificateChain(
      certificatesStore,
      certFromSignedData,
      false
    );

    // Verifica el certificado
    CertificateVerifier.verifyCertificate(
      certFromSignedData,
      additionalCerts,
      trustedCertificates,
      true,
      signDate
    );
    log.debug("Cadena de certificados verificada");
    // TODO: verificar si el certificado raíz está en nuestra lista de confianza
  }

  // Obtiene la cadena de certificados
  public static Set<X509Certificate> getCertificateChain(
    Store<X509CertificateHolder> certificatesStore,
    X509Certificate certFromSignedData,
    boolean includeRootCertificates
  ) throws CertificateException, NoSuchAlgorithmException, NoSuchProviderException {
    Collection<X509CertificateHolder> certificateHolders = certificatesStore.getMatches(null);
    Set<X509Certificate> additionalCerts = new HashSet<>();
    JcaX509CertificateConverter certificateConverter = new JcaX509CertificateConverter();
    for (X509CertificateHolder certHolder : certificateHolders) {
      X509Certificate certificate = certificateConverter.getCertificate(certHolder);
      if (!certificate.equals(certFromSignedData)) {
        additionalCerts.add(certificate);
      }
    }
    return additionalCerts;
  }

  /**
   * Verifica si el certificado es válido para sellado de tiempo.
   *
   * @param x509Certificate
   * @return Lista de advertencias
   */
  public static List<String> checkTimeStampCertificateUsage(X509Certificate x509Certificate)
    throws CertificateParsingException {
    List<String> extendedKeyUsage = x509Certificate.getExtendedKeyUsage();
    if (
      extendedKeyUsage != null &&
      !extendedKeyUsage.contains(KeyPurposeId.id_kp_timeStamping.toString())
    ) {
      return List.of(
        "El certificado del sello de tiempo no tiene contemplado en su uso el sellado de tiempo"
      );
    }
    return List.of();
  }

  /**
   * Verifica si el certificado es válido para responder.
   *
   * @param x509Certificate
   */
  public static void checkResponderCertificateUsage(X509Certificate x509Certificate)
    throws CertificateParsingException {
    List<String> extendedKeyUsage = x509Certificate.getExtendedKeyUsage();
    if (
      extendedKeyUsage != null &&
      !extendedKeyUsage.contains(KeyPurposeId.id_kp_OCSPSigning.toString())
    ) {
      log.error("El certificado no incluye el uso extendido para respuesta OCSP");
    }
  }

  /**
   * Similar a URL#openStream() pero seguirá la redirección de http a https.
   *
   * @param urlString URL a abrir
   * @return Stream de entrada
   */
  public static InputStream openURL(String urlString) throws MalformedURLException, IOException {
    URL url = new URL(urlString);
    if (!urlString.startsWith("http")) {
      return url.openStream();
    }
    // Establece la conexión HTTP
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    int responseCode = con.getResponseCode();
    log.info(responseCode + " " + con.getResponseMessage());
    
    // Maneja las redirecciones
    if (
      responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
      responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
      responseCode == HttpURLConnection.HTTP_SEE_OTHER
    ) {
      String location = con.getHeaderField("Location");
      if (
        urlString.startsWith("http://") &&
        location.startsWith("https://") &&
        urlString.substring(7).equals(location.substring(8))
      ) {
        log.info("redirección a " + location + " seguida");
        con.disconnect();
        con = (HttpURLConnection) new URL(location).openConnection();
      } else {
        log.info("redirección a " + location + " ignorada");
      }
    }
    return new ConnectedInputStream(con, con.getInputStream());
  }
}
