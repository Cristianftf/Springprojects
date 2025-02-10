package cu.xetid.cav.pdfsigner.validation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.operator.bc.BcRSAContentVerifierProviderBuilder;

public class PemValidator {

  public X509Certificate parse(InputStream pemInputStream)
    throws CertificateException, IOException, Exception {
    Reader reader = new InputStreamReader(pemInputStream, StandardCharsets.UTF_8);
    PEMParser pemParser = new PEMParser(reader);
    X509CertificateHolder certHolder = (X509CertificateHolder) pemParser.readObject();
    X509Certificate cert = new JcaX509CertificateConverter()
      .setProvider("BC")
      .getCertificate(certHolder);
    return cert;
  }
}
