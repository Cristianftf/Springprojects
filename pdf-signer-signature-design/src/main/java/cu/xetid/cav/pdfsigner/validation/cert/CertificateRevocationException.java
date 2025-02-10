package cu.xetid.cav.pdfsigner.validation.cert;

public class CertificateRevocationException extends CertificateVerificationException {

  public CertificateRevocationException(String message, Throwable cause) {
    super(message, cause);
  }

  public CertificateRevocationException(String message) {
    super(message);
  }

  public CertificateRevocationException() {
    super("Could not get revocation data");
  }
}
