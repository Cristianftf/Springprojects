package cu.xetid.cav.pdfsigner.validation;

import cu.xetid.cav.pdfsigner.pdfsigner.signature.SigUtils;
import cu.xetid.cav.pdfsigner.util.RSA;
import cu.xetid.cav.pdfsigner.validation.cert.CertificateRevocationException;
import cu.xetid.cav.pdfsigner.validation.cert.CertificateVerificationException;
import cu.xetid.cav.pdfsigner.validation.cert.CertificateVerifier;
import cu.xetid.cav.pdfsigner.validation.cert.RevokedCertificateException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.util.CollectionStore;
import org.springframework.lang.Nullable;

@Slf4j
public class Pkcs12Validator {

  public X509Certificate parse(
    InputStream pkcs12InputStream,
    String password
  ) throws Pkcs12ValidationException {
    List<X509Certificate> chain = null;
    try {
      String decryptedPassword = RSA.getInstance().decryptMessage(password);
      chain = getCertificateChain(pkcs12InputStream, decryptedPassword);
      if (chain == null) {
        throw new Exception();
      }
      return chain.get(0);
    } catch (IOException ex) {
      if (ex.getMessage().toLowerCase().contains("wrong password or corrupted file")) {
        throw new Pkcs12ValidationException(
          "La contraseña es incorrecta o el archivo está corrupto"
        );
      }
      throw new Pkcs12ValidationException("Archivo inválido o corrupto");
    } catch (Exception e) {
      throw new Pkcs12ValidationException("Archivo inválido o corrupto");
    }
  }

  public X509Certificate validate(
    InputStream pkcs12InputStream,
    String password,
    Set<X509Certificate> caTrustedCerts
  ) throws Pkcs12ValidationException {
    List<X509Certificate> chain = null;
    try {
      String decryptedPassword = RSA.getInstance().decryptMessage(password);
      chain = getCertificateChain(pkcs12InputStream, decryptedPassword);
      if (chain == null) {
        throw new Exception();
      }
    } catch (IOException ex) {
      if (ex.getMessage().toLowerCase().contains("wrong password or corrupted file")) {
        throw new Pkcs12ValidationException(
          "La contraseña es incorrecta o el archivo está corrupto"
        );
      }
      throw new Pkcs12ValidationException("Archivo inválido o corrupto");
    } catch (Exception e) {
      throw new Pkcs12ValidationException("Archivo inválido o corrupto");
    }

    boolean isSelfSigned = false;
    try {
      isSelfSigned = CertificateVerifier.isSelfSigned(chain.get(0));
    } catch (GeneralSecurityException ignored) {
      throw new Pkcs12ValidationException("Archivo inválido o corrupto");
    }
    if (isSelfSigned) {
      throw new Pkcs12ValidationException("El certificado es autofirmado");
    }

    try {
      CollectionStore<X509CertificateHolder> certificatesStore = new CollectionStore<>(
        chain
          .stream()
          .map(cert -> {
            try {
              return new JcaX509CertificateHolder(cert);
            } catch (CertificateEncodingException e) {
              e.printStackTrace();
              log.error(e.getMessage());
              return null;
            }
          })
          .filter(Objects::nonNull)
          .collect(Collectors.toList())
      );
      SigUtils.verifyCertificateChain(certificatesStore, chain.get(0), caTrustedCerts, new Date());
      return chain.get(0);
    } catch (CertificateRevokedException ex) {
      throw new Pkcs12ValidationException("El certificado es inválido porque fue revocado");
    } catch (CertificateRevocationException ex) {
      throw new Pkcs12ValidationException(
        "El certificado es válido pero no se pudo comprobar si ha sido revocado"
      );
    } catch (CertificateVerificationException ex) {
      throw new Pkcs12ValidationException(ex.getMessage());
    } catch (Exception ex) {
      if (ex instanceof RevokedCertificateException) {
        Date revocationTime = ((RevokedCertificateException) ex).getRevocationTime();
        String formattedDate = DateTimeFormatter
          .ofPattern("yyyy-MM-dd HH:mm:ss xx")
          .withZone(ZoneId.of("America/Havana"))
          .format(revocationTime.toInstant());
        throw new Pkcs12ValidationException(
          "El certificado fue revocado en la siguiente fecha: " + formattedDate
        );
      }
      throw new Pkcs12ValidationException("El certificado es inválido", ex);
    }
  }

  private @Nullable List<X509Certificate> getCertificateChain(
    InputStream pkcs12InputStream,
    String password
  )
    throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, NoSuchProviderException {
    KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
    keyStore.load(pkcs12InputStream, password.toCharArray());
    Enumeration<String> aliases = keyStore.aliases();
    List<X509Certificate> certificateChain = new ArrayList<>();
    while (aliases.hasMoreElements()) {
      String alias = aliases.nextElement();
      Certificate[] certificates = keyStore.getCertificateChain(alias);
      if (certificates != null) {
        Collections.addAll(
          certificateChain,
          Arrays
            .stream(certificates)
            .map(cert -> (X509Certificate) cert)
            .toArray(X509Certificate[]::new)
        );
      }
    }
    return certificateChain;
  }

  public void changePassword(
    InputStream pkcs12InputStream,
    String oldPassword,
    String newPassword,
    OutputStream outputStream
  )
    throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, Exception {
    String decryptedOldPassword = RSA.getInstance().decryptMessage(oldPassword);
    String decryptedNewPassword = RSA.getInstance().decryptMessage(newPassword);
    KeyStore newKs = KeyStore.getInstance("PKCS12");
    newKs.load(null, null);

    KeyStore ks = KeyStore.getInstance("PKCS12");
    ks.load(pkcs12InputStream, decryptedOldPassword.toCharArray());
    Enumeration<String> aliases = ks.aliases();

    while (aliases.hasMoreElements()) {
      String alias = aliases.nextElement();
      Key privateKey = ks.getKey(alias, decryptedOldPassword.toCharArray());
      Certificate[] certificateChain = ks.getCertificateChain(alias);
      newKs.setKeyEntry(alias, privateKey, decryptedNewPassword.toCharArray(), certificateChain);
    }

    newKs.store(outputStream, decryptedNewPassword.toCharArray());
  }
}
