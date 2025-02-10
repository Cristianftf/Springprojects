package cu.xetid.cav.pdfsigner.pdfsigner.signature;

// Importaciones necesarias para el funcionamiento de la clase
import cu.xetid.cav.pdfsigner.dto.OptionsDto;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.util.Matrix;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.springframework.util.FastByteArrayOutputStream;

// Anotación para habilitar el logging
@Slf4j
public class Signature extends BaseSignature {

  // Imagen de la firma
  @Setter
  private InputStream signatureImage;

  // Entidad firmante
  @Setter
  private String signerEntity;

  // Cargo del firmante
  @Setter
  private String signerJobTitle;

  // Opciones de configuración
  private OptionsDto optionsDto;

  // Constructor de la clase
  public Signature(KeyStore keyStore, char[] password, String tsaUrl, OptionsDto optionsDto)
    throws CertificateParsingException, CertificateExpiredException, CertificateNotYetValidException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, IOException {
    super(keyStore, password, tsaUrl);
    this.optionsDto = optionsDto;
  }

  // Método para firmar el documento de forma separada
  public void signDetached(PDDocument document, OutputStream output)
    throws IOException, TranscoderException {
    log.debug("creando un diseño de firma");
    SignatureDesign signatureDesign = new SignatureDesign(optionsDto);
    log.debug("estableciendo la imagen de la firma");
    signatureDesign.setSignatureImage(signatureImage);
    log.debug("creando el rectángulo");
    PDRectangle rectangle = signatureDesign.createSignatureRectangle(document);

    log.debug("creando el PDSignature");
    PDSignature pdSignature = new PDSignature();
    // filtro por defecto
    log.debug("estableciendo filtro");
    pdSignature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
    // subfiltro para firmas básicas y PAdES Parte 2
    log.debug("estableciendo subfiltro");
    pdSignature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);

    log.debug("Extrayendo nombre del firmante");
    String signerName = this.extractSignerNameFromCertificate();
    log.debug("Estableciendo nombre del firmante");
    pdSignature.setName(signerName);
    pdSignature.setLocation("Cuba");
    pdSignature.setReason("Certificacion");

    // fecha de firma, necesaria para una firma válida
    log.debug("Estableciendo fecha de firma");
    pdSignature.setSignDate(Calendar.getInstance());

    // registrar diccionario de firma e interfaz de firma
    SignatureInterface signatureInterface = this;
    log.debug("Creando plantilla de firma visual");
    InputStream visualSignatureTemplate = signatureDesign.createVisualSignatureTemplate(
      document,
      rectangle,
      pdSignature
    );
    log.debug("Creando opciones de firma");
    SignatureOptions signatureOptions = new SignatureOptions();
    log.debug("estableciendo firma visual");
    signatureOptions.setVisualSignature(visualSignatureTemplate);
    int lastPageIndex = document.getNumberOfPages() - 1;
    signatureOptions.setPage(lastPageIndex);
    signatureOptions.setPreferredSignatureSize(20000);

    // registrar diccionario de firma
    log.debug("añadiendo firma");
    document.addSignature(pdSignature, signatureInterface, signatureOptions);

    log.debug("guardando incremental");
    document.saveIncremental(output);

    // No cerrar signatureOptions antes de guardar, porque algunos objetos COSStream
    // dentro se transfieren al documento firmado.
    // No permitir que signatureOptions salga del alcance antes de guardar, porque entonces
    // el COSDocument en las opciones de firma podría ser cerrado por gc, lo que cerraría
    // los objetos COSStream prematuramente.
    log.debug("cerrando opciones de firma");
    IOUtils.closeQuietly(signatureOptions);
    log.debug("cerrando documento");
    IOUtils.closeQuietly(document);
  }

  // Método para extraer el nombre del firmante del certificado
  private String extractSignerNameFromCertificate() {
    X509Certificate cert = (X509Certificate) getCertificateChain()[0];

    X500Name x500Name = new X500Name(cert.getSubjectX500Principal().getName());
    RDN cn = x500Name.getRDNs(BCStyle.CN)[0];
    return IETFUtils.valueToString(cn.getFirst().getValue());
  }
  // private PDType1Font getFontBold() {
  //   return PDType1Font.HELVETICA_BOLD;
  // }

  // private float computeSignatureYPosition(PDDocument doc) throws IOException {
  //   PDPage lastPage = getDocumentLastPage(doc);
  //   float lastPageHeight = lastPage.getCropBox().getHeight();

  //   float lastTextY = getLastTextYFromDocumentLastPage(doc);
  //   float lastAnnotationY = lastPage
  //     .getAnnotations()
  //     .stream()
  //     .map((PDAnnotation annotation) -> annotation.getRectangle().getLowerLeftY())
  //     .reduce(Float.MAX_VALUE, Math::min);
  //   if (lastAnnotationY < Float.MAX_VALUE) {
  //     lastAnnotationY = lastPageHeight - lastAnnotationY;
  //   } else {
  //     lastAnnotationY = 0;
  //   }

  //   float yPosition = Math.max(lastTextY, lastAnnotationY) + SIGNATURE_MARGIN_TOP;
  //   return (yPosition >= (lastPageHeight - SIGNATURE_HEIGHT - SIGNATURE_MARGIN_BOTTOM))
  //     ? -1
  //     : yPosition;
  // }

  // private float getLastTextYFromDocumentLastPage(PDDocument doc) throws IOException {
  //   PDFTextPositionStripper stripper = new PDFTextPositionStripper();
  //   stripper.setSortByPosition(true);
  //   stripper.setStartPage(doc.getNumberOfPages());
  //   stripper.writeText(doc, Writer.nullWriter());

  //   List<TextPosition> textPositions = stripper.getTextPositions();
  //   if (textPositions != null) {
  //     TextPosition lastTextPosition = textPositions.get(textPositions.size() - 1);
  //     return lastTextPosition.getY();
  //   }
  //   return 0;
  // }
}
