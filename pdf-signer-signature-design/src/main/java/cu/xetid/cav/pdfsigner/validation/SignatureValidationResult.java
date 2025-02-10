package cu.xetid.cav.pdfsigner.validation;

import java.util.Calendar;
import java.util.Set;
import lombok.Getter;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;

import com.itextpdf.text.pdf.security.PdfPKCS7;

public class SignatureValidationResult {

  @Getter
  final String name;

  final boolean isValid;

  @Getter
  final Set<String> errors;

  @Getter
  final Set<String> warnings;

  @Getter
  final Set<String> infoMessages;

  @Getter
  final Calendar signedAt;

  @Getter
  final Set<String> caChain;

  public SignatureValidationResult(PdfPKCS7 pdfPKCS7, PdfSignatureValidationResult result) {
    this.name = pdfPKCS7.getSignName();
    this.caChain = result.caChain;
    this.signedAt = pdfPKCS7.getSignDate();
    errors = result.errors;
    warnings = result.warnings;
    infoMessages = result.infoMessages;
    isValid = errors.isEmpty();
  }

  public boolean getIsValid() {
    return isValid;
  }
}
