package cu.xetid.cav.pdfsigner.pdfsigner.signature;

// Importación de clases necesarias para el diseño de la firma
import cu.xetid.cav.pdfsigner.dto.OptionsDto;
import cu.xetid.cav.pdfsigner.util.MarkdownToContentStream;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import javax.imageio.ImageIO;
import lombok.Setter;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
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
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.apache.pdfbox.util.Matrix;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.springframework.util.FastByteArrayOutputStream;

public class SignatureDesign {

  // Constantes que definen las dimensiones y posiciones de la firma
  private static final int SIGNATURE_WIDTH = 400;
  private static final int SIGNATURE_HEIGHT = 60;
  private static final int SIGNATURE_IMAGE_SIZE = 38;
  private static final int SIGNATURE_TEXT_HEIGHT = 52;
  private static final int SIGNATURE_POSITION_Y = 20;
  private static final int SIGNATURE_MARGIN_TOP_NEW_PAGE = 60;
  private static final float SIGNATURE_MARGIN_BOTTOM = 10;
  private static final int SIGNATURE_FONT_SIZE = 8;
  private static final int SIGNATURE_POSITION_X = 60;
  private OptionsDto optionsDto;

  // Imagen de la firma que se insertará
  @Setter
  private InputStream signatureImage;

  // Constructor que recibe las opciones de configuración
  public SignatureDesign(OptionsDto optionsDto) {
    this.optionsDto = optionsDto;
  }

  // Método que crea el rectángulo donde se ubicará la firma
  public PDRectangle createSignatureRectangle(PDDocument document) throws IOException {
    // Crea un rectángulo con las dimensiones especificadas
    Rectangle2D humanRect = new Rectangle2D.Float(
      optionsDto.signaturePositionX != 0 ? optionsDto.signaturePositionX : SIGNATURE_POSITION_X,
      optionsDto.signaturePositionY != 0 ? optionsDto.signaturePositionY : SIGNATURE_POSITION_Y,
      optionsDto.signatureWidth != 0 ? optionsDto.signatureWidth : SIGNATURE_WIDTH,
      optionsDto.signatureHeight != 0 ? optionsDto.signatureHeight : SIGNATURE_HEIGHT
    );

    float x = (float) humanRect.getX();
    float y = (float) humanRect.getY();
    float width = (float) humanRect.getWidth();
    float height = (float) humanRect.getHeight();
    PDPage page = document.getPage(0);
    PDRectangle pageRect = page.getCropBox();
    PDRectangle rect = new PDRectangle();

    // Ajusta la posición de la firma según la rotación de la página
    switch (page.getRotation()) {
      case 90:
        rect.setLowerLeftY(x);
        rect.setUpperRightY(x + width);
        rect.setLowerLeftX(y);
        rect.setUpperRightX(y + height);
        break;
      case 180:
        rect.setUpperRightX(pageRect.getWidth() - x);
        rect.setLowerLeftX(pageRect.getWidth() - x - width);
        rect.setLowerLeftY(y);
        rect.setUpperRightY(y + height);
        break;
      case 270:
        rect.setLowerLeftY(pageRect.getHeight() - x - width);
        rect.setUpperRightY(pageRect.getHeight() - x);
        rect.setLowerLeftX(pageRect.getWidth() - y - height);
        rect.setUpperRightX(pageRect.getWidth() - y);
        break;
      case 0:
      default:
        rect.setLowerLeftX(x);
        rect.setUpperRightX(x + width);
        rect.setLowerLeftY(pageRect.getHeight() - y - height);
        rect.setUpperRightY(pageRect.getHeight() - y);
        break;
    }

    return rect;
  }

  /**
   * Crea una plantilla de documento PDF con firma vacía y la devuelve como stream
   */
  public InputStream createVisualSignatureTemplate(
    PDDocument srcDoc,
    PDRectangle rect,
    PDSignature signature
  ) throws IOException, TranscoderException {
    PDPage firstPage = srcDoc.getPage(0);

    try (PDDocument doc = new PDDocument()) {
      // Crea una nueva página con las mismas dimensiones que la primera
      PDPage page = new PDPage(firstPage.getMediaBox());
      doc.addPage(page);
      
      // Configura el formulario de firma
      PDAcroForm acroForm = new PDAcroForm(doc);
      doc.getDocumentCatalog().setAcroForm(acroForm);
      PDSignatureField signatureField = new PDSignatureField(acroForm);
      PDAnnotationWidget widget = signatureField.getWidgets().get(0);
      List<PDField> acroFormFields = acroForm.getFields();
      acroForm.setSignaturesExist(true);
      acroForm.setAppendOnly(true);
      acroForm.getCOSObject().setDirect(true);
      acroFormFields.add(signatureField);

      widget.setRectangle(rect);

      // Configura el tamaño de la fuente
      int fontSize = optionsDto.signatureFontSize != 0
        ? optionsDto.signatureFontSize
        : SIGNATURE_FONT_SIZE;
      
      // Crea el formulario visual de la firma
      PDStream stream = new PDStream(doc);
      PDFormXObject form = new PDFormXObject(stream);
      PDResources res = new PDResources();
      form.setResources(res);
      form.setFormType(1);
      PDRectangle bbox = new PDRectangle(rect.getWidth(), rect.getHeight());
      float height = bbox.getHeight();
      Matrix initialScale = null;
      
      // Ajusta la rotación del formulario según la página
      int rotation = firstPage.getRotation();
      switch (rotation) {
        case 90:
          form.setMatrix(AffineTransform.getQuadrantRotateInstance(1));
          initialScale =
            Matrix.getScaleInstance(
              bbox.getWidth() / bbox.getHeight(),
              bbox.getHeight() / bbox.getWidth()
            );
          height = bbox.getWidth();
          break;
        case 180:
          form.setMatrix(AffineTransform.getQuadrantRotateInstance(2));
          break;
        case 270:
          form.setMatrix(AffineTransform.getQuadrantRotateInstance(3));
          initialScale =
            Matrix.getScaleInstance(
              bbox.getWidth() / bbox.getHeight(),
              bbox.getHeight() / bbox.getWidth()
            );
          height = bbox.getWidth();
          break;
        case 0:
        default:
          break;
      }
      form.setBBox(bbox);

      // Configura la apariencia del widget de firma
      PDAppearanceDictionary appearance = new PDAppearanceDictionary();
      appearance.getCOSObject().setDirect(true);
      PDAppearanceStream appearanceStream = new PDAppearanceStream(form.getCOSObject());
      appearance.setNormalAppearance(appearanceStream);
      widget.setAppearance(appearance);

      try (PDPageContentStream cs = new PDPageContentStream(doc, appearanceStream)) {
        // Aplica la escala inicial si es necesario
        if (initialScale != null) {
          cs.transform(initialScale);
        }

        // Inserta la imagen de la firma si existe
        if (signatureImage != null) {
          cs.saveGraphicsState();
          BufferedImage bim = getSignatureImage();
          PDImageXObject img = LosslessFactory.createFromImage(doc, bim);

          float aspectRatio = img.getWidth() * 1.0f / img.getHeight();
          int signatureImageSize = optionsDto.signatureImageSize != 0
            ? optionsDto.signatureImageSize
            : SIGNATURE_IMAGE_SIZE;
          float computedImageHeight = signatureImageSize / aspectRatio;
          cs.drawImage(
            img,
            0 + optionsDto.signatureImageRelativePositionX,
            height - computedImageHeight + optionsDto.signatureImageRelativePositionY,
            signatureImageSize,
            computedImageHeight
          );
          cs.restoreGraphicsState();
        }
        
        // Configura el formato de fecha y hora
        Locale locale = Locale.forLanguageTag("es-ES");
        DateTimeFormatter formatter = DateTimeFormatter
          .ofLocalizedDateTime(FormatStyle.MEDIUM)
          .withLocale(locale);
        String date = signature
          .getSignDate()
          .getTime()
          .toInstant()
          .atZone(ZoneId.systemDefault())
          .format(formatter);

        // Calcula la posición del texto de la firma
        int relativePositionX = optionsDto.signatureDescriptionRelativePositionX;
        int relativePositionY = (int) height -
        optionsDto.signatureDescriptionRelativePositionY -
        fontSize;

        // Procesa y dibuja el texto de la firma
        MarkdownToContentStream.process(
          cs,
          optionsDto.signatureDescription != null ? optionsDto.signatureDescription : "",
          date,
          relativePositionX,
          relativePositionY,
          fontSize
        );
      }

      // Guarda el documento y lo devuelve como stream
      var baos = new FastByteArrayOutputStream();
      doc.save(baos);
      return baos.getInputStream();
    }
  }

  // Método auxiliar para procesar la imagen de la firma
  private BufferedImage getSignatureImage() throws IOException, TranscoderException {
    var buffer = new FastByteArrayOutputStream();
    signatureImage.transferTo(buffer);

    // Intenta leer la imagen directamente
    var bim = ImageIO.read(buffer.getInputStream());
    // Si no es posible, intenta convertirla a PNG primero
    if (bim == null) {
      var pngTranscoder = new PNGTranscoder();
      var os = new FastByteArrayOutputStream();
      pngTranscoder.transcode(
        new TranscoderInput(buffer.getInputStream()),
        new TranscoderOutput(os)
      );
      bim = ImageIO.read(os.getInputStream());
    }
    return bim;
  }
}
