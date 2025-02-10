package cu.xetid.cav.pdfsigner.pdfsigner.signature;

// Importaciones necesarias para el funcionamiento de la clase
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.Attributes;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;

/**
 * Esta clase envuelve el TSAClient y el trabajo que debe realizarse con él.
 * Como agregar marcas de tiempo firmadas a una firma, o crear un atributo de marca de tiempo CMS
 *
 * @author Others
 * @author Alexis Suter
 */
@Slf4j
public class ValidationTimeStamp {

  // Cliente TSA para manejar las operaciones de marca de tiempo
  private TSAClient tsaClient;

  /**
   * @param tsaUrl La URL donde se realizará la solicitud TS
   * @throws NoSuchAlgorithmException
   * @throws MalformedURLException
   */
  public ValidationTimeStamp(String tsaUrl) throws NoSuchAlgorithmException, MalformedURLException {
    // Si se proporciona una URL, inicializa el cliente TSA con SHA-256
    if (tsaUrl != null) {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      this.tsaClient = new TSAClient(new URL(tsaUrl), null, null, digest);
    }
  }

  /**
   * Crea un token de marca de tiempo firmado para el flujo de entrada dado
   *
   * @param content InputStream del contenido a firmar
   * @return el array de bytes del token de marca de tiempo
   * @throws IOException
   */
  public byte[] getTimeStampToken(InputStream content) throws IOException {
    // Convierte el contenido a bytes y obtiene el token de marca de tiempo
    return tsaClient.getTimeStampToken(IOUtils.toByteArray(content));
  }

  /**
   * Extiende los datos firmados CMS con marca de tiempo para todos los firmantes
   *
   * @param signedData Datos CMS firmados generados
   * @return CMSSignedData Datos CMS firmados extendidos
   * @throws IOException
   */
  public CMSSignedData addSignedTimeStamp(CMSSignedData signedData) throws IOException {
    try {
      // Obtiene el almacén de información de firmantes
      SignerInformationStore signerStore = signedData.getSignerInfos();
      List<SignerInformation> newSigners = new ArrayList<SignerInformation>();

      log.debug("agregando firmantes al array");
      // Itera sobre cada firmante para agregar la marca de tiempo
      for (SignerInformation signer : signerStore.getSigners()) {
        // Esto agrega una marca de tiempo a cada firmante en sus atributos no firmados
        newSigners.add(signTimeStamp(signer));
      }

      // Como se crea nueva información de firmante, se debe crear un nuevo SignerInfoStore
      // y reemplazarlo en signedData, lo que crea un nuevo objeto signedData
      log.debug("reemplazando signedData con los nuevos firmantes");
      CMSSignedData cMSSignedData = CMSSignedData.replaceSigners(
        signedData,
        new SignerInformationStore(newSigners)
      );
      log.debug("reemplazado signedData con los nuevos firmantes");
      return cMSSignedData;
    } catch (Exception e) {
      log.error("Error al firmar con TSA: " + e.getMessage());
      throw e;
    }
  }

  /**
   * Extiende la información del firmante CMS con el token de marca de tiempo en los atributos no firmados
   *
   * @param signer información sobre el firmante
   * @return información sobre SignerInformation
   * @throws IOException
   */
  private SignerInformation signTimeStamp(SignerInformation signer) throws IOException {
    log.debug("firmando marca de tiempo: " + signer.getSID());
    // Obtiene los atributos no firmados actuales
    AttributeTable unsignedAttributes = signer.getUnsignedAttributes();

    ASN1EncodableVector vector = new ASN1EncodableVector();
    log.debug("creado un nuevo vector");
    if (unsignedAttributes != null) {
      vector = unsignedAttributes.toASN1EncodableVector();
    }

    log.debug("Obteniendo token de marca de tiempo");
    // Obtiene el token de marca de tiempo para la firma
    byte[] token = tsaClient.getTimeStampToken(signer.getSignature());
    ASN1ObjectIdentifier oid = PKCSObjectIdentifiers.id_aa_signatureTimeStampToken;
    log.debug("Creando marca de tiempo de firma");
    ASN1Encodable signatureTimeStamp = new Attribute(
      oid,
      new DERSet(ASN1Primitive.fromByteArray(token))
    );

    log.debug("Agregando marca de tiempo de firma al vector");
    vector.add(signatureTimeStamp);
    Attributes signedAttributes = new Attributes(vector);

    // No hay otra forma de cambiar los atributos no firmados de la información del firmante.
    // El resultado nunca es nulo, siempre se devuelve un nuevo SignerInformation
    log.debug("reemplazando atributos no firmados");
    SignerInformation signerInformation = SignerInformation.replaceUnsignedAttributes(
      signer,
      new AttributeTable(signedAttributes)
    );
    log.debug("reemplazados los atributos no firmados");
    return signerInformation;
  }
}
