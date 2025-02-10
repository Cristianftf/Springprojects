// Declaración del paquete al que pertenece la clase
package cu.xetid.cav.pdfsigner.pdfsigner.signature;

// Importación de las clases necesarias
import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

// Clase que extiende de PDFTextStripper para obtener las posiciones del texto en un PDF
public class PDFTextPositionStripper extends PDFTextStripper {

  // Variable para almacenar las posiciones del texto
  private List<TextPosition> textPositions;

  // Constructor de la clase que llama al constructor de la clase padre
  public PDFTextPositionStripper() throws IOException {
    super();
  }

  // Sobrescribe el método writeString para capturar las posiciones del texto
  @Override
  protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
    super.writeString(text, textPositions);
    this.textPositions = textPositions;
  }

  // Método getter para obtener las posiciones del texto almacenadas
  public List<TextPosition> getTextPositions() {
    return textPositions;
  }
}