package cu.xetid.cav.pdfsigner.pdfsigner.signature;

// Importaciones necesarias para el funcionamiento de la clase
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.pdfbox.io.IOUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSTypedData;

// Clase que implementa CMSTypedData para procesar flujos de entrada
public class CMSProcessableInputStream implements CMSTypedData {

  // Flujo de entrada para los datos
  private final InputStream in;
  // Identificador del tipo de contenido
  private final ASN1ObjectIdentifier contentType;

  // Constructor que inicializa con un flujo de entrada
  CMSProcessableInputStream(InputStream is) {
    this(new ASN1ObjectIdentifier(CMSObjectIdentifiers.data.getId()), is);
  }

  // Constructor que inicializa con un tipo y flujo de entrada específicos
  CMSProcessableInputStream(ASN1ObjectIdentifier type, InputStream is) {
    contentType = type;
    in = is;
  }

  // Obtiene el contenido del flujo de entrada
  @Override
  public Object getContent() {
    return in;
  }

  // Escribe el contenido en el flujo de salida
  @Override
  public void write(OutputStream out) throws IOException, CMSException {
    // Lee el contenido solo una vez
    IOUtils.copy(in, out);
    in.close();
  }

  // Obtiene el tipo de contenido
  @Override
  public ASN1ObjectIdentifier getContentType() {
    return contentType;
  }
}
