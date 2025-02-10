package cu.xetid.cav.pdfsigner.validation;

import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;

public class PdfSignatureValidationResult {

  @Getter
  final Set<String> errors;

  @Getter
  final Set<String> warnings;

  @Getter
  final Set<String> infoMessages;

  @Getter
  final Set<String> caChain;

  PdfSignatureValidationResult() {
    errors = new LinkedHashSet<>();
    warnings = new LinkedHashSet<>();
    infoMessages = new LinkedHashSet<>();
    caChain = new LinkedHashSet<>();
  }

  public PdfSignatureValidationResult(
    Set<String> errors,
    Set<String> warnings,
    Set<String> infoMessages
  ) {
    this.errors = errors;
    this.warnings = warnings;
    this.infoMessages = infoMessages;
    caChain = new LinkedHashSet<>();
  }

  public PdfSignatureValidationResult(
    Set<String> errors,
    Set<String> warnings,
    Set<String> infoMessages,
    Set<String> caChain
  ) {
    this.errors = errors;
    this.warnings = warnings;
    this.infoMessages = infoMessages;
    this.caChain = caChain;
  }

  public static PdfSignatureValidationResultBuilder builder() {
    return new PdfSignatureValidationResultBuilder();
  }

  public static class PdfSignatureValidationResultBuilder {

    private final Set<String> errors;
    private final Set<String> warnings;
    private final Set<String> infoMessages;

    PdfSignatureValidationResultBuilder() {
      errors = new LinkedHashSet<>();
      warnings = new LinkedHashSet<>();
      infoMessages = new LinkedHashSet<>();
    }

    public PdfSignatureValidationResultBuilder withError(String error) {
      errors.add(error);
      return this;
    }

    public PdfSignatureValidationResultBuilder withWarning(String warning) {
      warnings.add(warning);
      return this;
    }

    public PdfSignatureValidationResultBuilder withInfo(String info) {
      infoMessages.add(info);
      return this;
    }

    public PdfSignatureValidationResult build() {
      return new PdfSignatureValidationResult(errors, warnings, infoMessages);
    }
  }
}
