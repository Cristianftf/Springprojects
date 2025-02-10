package cu.xetid.cav.pdfsigner.validation.cert;

import java.security.cert.X509Certificate;
import lombok.Data;
import org.bouncycastle.cert.ocsp.OCSPResp;

@Data
public class SignatureValidationCacheItem {

  private final OCSPResp ocspResponse;
  private final X509Certificate ocspResponderCertificate;
}
