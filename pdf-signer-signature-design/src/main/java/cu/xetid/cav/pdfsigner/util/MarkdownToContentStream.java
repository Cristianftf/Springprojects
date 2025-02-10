package cu.xetid.cav.pdfsigner.util;

import cu.xetid.cav.pdfsigner.dto.OptionsDto;
import java.awt.Color;
import java.io.IOException;
import java.util.Stack;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;

public class MarkdownToContentStream {

  // Método que procesa el texto markdown y lo convierte en contenido del PDF
  public static void process(
    PDPageContentStream cs,
    String markdown,
    String date,
    int relativePositionX,
    int relativePositionY,
    int fontSize
  ) throws IOException {
    // Crea un parser para procesar el markdown
    Parser parser = Parser.builder().build();
    // Parsea el texto markdown a un árbol de nodos
    Node root = parser.parse(markdown);
    // Convierte el árbol de nodos a una pila
    Stack<Node> stack = toStack(root);

    // Define las fuentes a utilizar
    PDFont font = PDType1Font.HELVETICA;
    PDFont fontCursive = PDType1Font.HELVETICA_OBLIQUE;
    PDFont fontBold = PDType1Font.HELVETICA_BOLD;

    // Inicia el bloque de texto en el PDF
    cs.beginText();
    // Establece el color negro para el texto
    cs.setNonStrokingColor(Color.black);
    // Establece la posición inicial del texto
    cs.newLineAtOffset(relativePositionX, relativePositionY);
    // Establece el espaciado entre líneas
    cs.setLeading(8 * 1.5f);

    // Procesa cada nodo de la pila
    while (stack.size() != 0) {
      Node item = stack.pop();
      // Ignora los nodos tipo Document
      if (item instanceof Document) continue;
      // Configura el formato para párrafos
      if (item instanceof Paragraph) {
        cs.setFont(font, fontSize);
        continue;
      }
      // Procesa nodos de texto
      if (item instanceof Text) {
        Text text = (Text) item;
        cs.showText(text.getLiteral());
        cs.setFont(font, fontSize);
        continue;
      }
      // Configura el formato para encabezados
      if (item instanceof Heading) {
        Heading heading = (Heading) item;
        cs.setFont(fontBold, fontSize + 7 - heading.getLevel());
        continue;
      }
      // Configura el formato para texto en cursiva
      if (item instanceof Emphasis) {
        cs.setFont(fontCursive, fontSize);
        continue;
      }
      // Configura el formato para texto en negrita
      if (item instanceof StrongEmphasis) {
        cs.setFont(fontBold, fontSize);
        continue;
      }
      // Procesa los saltos de línea suaves
      if (item instanceof SoftLineBreak) {
        cs.newLine();
        continue;
      }
    }

    // Si el markdown está vacío, finaliza el bloque de texto
    if (markdown == "") {
      cs.endText();
      return;
    }
    // Agrega la fecha al final del texto
    cs.newLine();
    cs.setFont(font, fontSize);
    cs.showText("Fecha: ");
    cs.setFont(fontBold, fontSize);
    cs.showText(date);
    cs.setFont(font, fontSize);
    cs.newLine();
    cs.endText();
  }

  // Método auxiliar que convierte un árbol de nodos en una pila
  private static Stack<Node> toStack(Node node) {
    Node lastChild = node.getLastChild();
    Stack<Node> stack = new Stack<Node>();

    // Procesa recursivamente todos los nodos hijos
    while (lastChild != null) {
      stack.addAll(toStack(lastChild));
      lastChild = lastChild.getPrevious();
    }

    // Agrega el nodo actual a la pila
    stack.add(node);

    return stack;
  }
}
