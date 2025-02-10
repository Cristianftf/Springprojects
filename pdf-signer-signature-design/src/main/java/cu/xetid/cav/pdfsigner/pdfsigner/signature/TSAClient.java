package cu.xetid.cav.pdfsigner.pdfsigner.signature;

// Importaciones necesarias para el funcionamiento de la clase
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.SecureRandom;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampToken;

/**
 * Cliente de Autoridad de Sellado de Tiempo (TSA) [RFC 3161].
 *
 * @author Vakhtang Koroghlishvili
 * @author John Hewson
 */
@Slf4j
public class TSAClient {

  // Variables de clase para almacenar la URL del servicio TSA, credenciales y algoritmo de resumen
  private final URL url;
  private final String username;
  private final String password;
  private final MessageDigest digest;

  /**
   * @param url      la URL del servicio TSA
   * @param username nombre de usuario del TSA
   * @param password contraseña del TSA
   * @param digest   el algoritmo de resumen a utilizar
   */
  public TSAClient(URL url, String username, String password, MessageDigest digest) {
    this.url = url;
    this.username = username;
    this.password = password;
    this.digest = digest;
  }

  /**
   * @param messageImprint huella del contenido del mensaje
   * @return el token de sello de tiempo codificado
   * @throws IOException si hubo un error con la conexión o datos del servidor TSA,
   *                     o si la respuesta del sello de tiempo no pudo ser validada
   */
  public byte[] getTimeStampToken(byte[] messageImprint) throws IOException {
    log.debug("Obteniendo token de sello de tiempo");
    // Reinicia el digest y calcula el hash del mensaje
    digest.reset();
    byte[] hash = digest.digest(messageImprint);

    // Genera un número aleatorio criptográfico de 32 bits
    SecureRandom random = new SecureRandom();
    int nonce = random.nextInt();

    // Genera la solicitud TSA
    log.debug("Generando solicitud TSA");
    TimeStampRequestGenerator tsaGenerator = new TimeStampRequestGenerator();
    tsaGenerator.setCertReq(true);
    ASN1ObjectIdentifier oid = getHashObjectIdentifier(digest.getAlgorithm());
    TimeStampRequest request = tsaGenerator.generate(oid, hash, BigInteger.valueOf(nonce));

    // Obtiene la respuesta TSA
    log.debug("Obteniendo respuesta TSA");
    byte[] tsaResponse = getTSAResponse(request.getEncoded());

    TimeStampResponse response;
    try {
      log.debug("Respuesta TSA");
      log.debug(tsaResponse.toString());
      response = new TimeStampResponse(tsaResponse);
      log.debug("Creada respuesta de sello de tiempo");
      response.validate(request);
    } catch (TSPException e) {
      log.error("Error creando la respuesta del sello de tiempo");
      throw new IOException(e);
    }

    log.debug("Obteniendo token de sello de tiempo");
    TimeStampToken token = response.getTimeStampToken();
    if (token == null) {
      log.error(
        "La respuesta de " +
        url +
        " no tiene un token de sello de tiempo, estado: " +
        response.getStatus() +
        " (" +
        response.getStatusString() +
        ")"
      );
      throw new IOException(
        "La respuesta de " +
        url +
        " no tiene un token de sello de tiempo, estado: " +
        response.getStatus() +
        " (" +
        response.getStatusString() +
        ")"
      );
    }

    return token.getEncoded();
  }

  // Obtiene los datos de respuesta para la solicitud TimeStampRequest codificada
  // Lanza IOException si no se puede establecer una conexión con el TSA
  private byte[] getTSAResponse(byte[] request) throws IOException {
    log.debug("Abriendo conexión al servidor TSA");

    // TODO: agregar soporte para servidores proxy
    URLConnection connection = url.openConnection();
    connection.setDoOutput(true);
    connection.setDoInput(true);
    connection.setRequestProperty("Content-Type", "application/timestamp-query");

    // Configura las credenciales si están disponibles
    if (username != null && password != null && !username.isEmpty() && !password.isEmpty()) {
      connection.setRequestProperty(username, password);
    }

    // Lee la respuesta
    OutputStream output = null;
    try {
      output = connection.getOutputStream();
      output.write(request);
      IOUtils.closeQuietly(output);
    } catch (Exception e) {
      log.error(e.getMessage());
      throw e;
    }

    log.debug("Esperando respuesta del servidor TSA");

    InputStream input = null;
    byte[] response;
    try {
      input = connection.getInputStream();
      response = IOUtils.toByteArray(input);
      IOUtils.closeQuietly(input);
    } catch (Exception e) {
      log.error(e.getMessage());
      throw e;
    }

    log.debug("Respuesta recibida del servidor TSA");

    return response;
  }

  // Retorna el OID ASN.1 del algoritmo hash proporcionado
  private ASN1ObjectIdentifier getHashObjectIdentifier(String algorithm) {
    switch (algorithm) {
      case "MD2":
        return new ASN1ObjectIdentifier(PKCSObjectIdentifiers.md2.getId());
      case "MD5":
        return new ASN1ObjectIdentifier(PKCSObjectIdentifiers.md5.getId());
      case "SHA-1":
        return new ASN1ObjectIdentifier(OIWObjectIdentifiers.idSHA1.getId());
      case "SHA-224":
        return new ASN1ObjectIdentifier(NISTObjectIdentifiers.id_sha224.getId());
      case "SHA-256":
        return new ASN1ObjectIdentifier(NISTObjectIdentifiers.id_sha256.getId());
      case "SHA-384":
        return new ASN1ObjectIdentifier(NISTObjectIdentifiers.id_sha384.getId());
      case "SHA-512":
        return new ASN1ObjectIdentifier(NISTObjectIdentifiers.id_sha512.getId());
      default:
        return new ASN1ObjectIdentifier(algorithm);
    }
  }
}
