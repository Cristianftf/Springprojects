package cu.xetid.cav.pdfsigner.validation;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.CertificateRevokedException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.encryption.SecurityProvider;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.util.CollectionStore;
import org.bouncycastle.util.Selector;
import org.bouncycastle.util.Store;
import org.springframework.lang.NonNull;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.security.PdfPKCS7;

import cu.xetid.cav.pdfsigner.pdfsigner.signature.SigUtils;
import cu.xetid.cav.pdfsigner.validation.cert.CertificateRevocationException;
import cu.xetid.cav.pdfsigner.validation.cert.CertificateVerificationException;
import cu.xetid.cav.pdfsigner.validation.cert.CertificateVerifier;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class PdfValidator {

  public Map<String, ?> validate(File file, Set<X509Certificate> trustedCertificates)
    throws Exception {
    PdfReader reader = new PdfReader(file.getAbsolutePath());
    AcroFields acroFields = reader.getAcroFields();
    List<String> names = acroFields.getSignatureNames();
    List<SignatureValidationResult> signatures = new ArrayList<>();
    log.debug("finding " + names.size() + " signatures");
    for (String name: names) {
      try {
        PdfPKCS7 pdfPKCS7 = acroFields.verifySignature(name);
        PdfSignatureValidationResult result;
        result = verifyPKCS7(pdfPKCS7, trustedCertificates);
        signatures.add(new SignatureValidationResult(pdfPKCS7, result));
      } catch (Exception e) {
        log.error("exception validating sign " + name + " " + e.getMessage());
      }
    }
    // PDDocument document = PDDocument.load(file);
    // log.debug("Creating the signatures list");
    // List<SignatureValidationResult> signatures = document
    //   .getSignatureDictionaries()
    //   .stream()
    //   .map(sig -> {
    //     byte[] contents = sig.getContents();
    //     byte[] signedContent = new byte[0];
    //     try {
    //       signedContent = sig.getSignedContent(new FileInputStream(file));
    //     } catch (IOException ignored) {}

    //     PdfSignatureValidationResult result = verifyPKCS7(signedContent, contents, sig, trustedCertificates);
    //     return new SignatureValidationResult(sig, result);
    //   })
    //   .collect(Collectors.toList());
    // document.close();

    var isValid = signatures
      .stream()
      .map(SignatureValidationResult::getIsValid)
      .reduce(true, (acc, curr) -> acc && curr);
    var hasWarnings = signatures
      .stream()
      .map(SignatureValidationResult::getWarnings)
      .map(set -> !set.isEmpty())
      .reduce(false, (acc, curr) -> acc || curr);

    return Map.ofEntries(
      Map.entry("isValid", isValid),
      Map.entry("hasWarnings", hasWarnings),
      Map.entry("verifiedAt", Calendar.getInstance().getTime()),
      Map.entry("signatures", signatures)
    );
  }

  /**
   * Verify a PKCS7 signature.
   *
   * @param byteArray the byte sequence that has been signed
   * @param contents  the /Contents field as a COSString
   * @param sig       the PDF signature (the /V dictionary)
   * @return
   * @throws CMSException
   * @throws OperatorCreationException
   * @throws IOException
   * @throws GeneralSecurityException
   * @throws TSPException
   */
  private @NonNull PdfSignatureValidationResult verifyPKCS7(
    PdfPKCS7 pdfPKCS7,
    // byte[] byteArray,
    // byte[] contents,
    // PDSignature sig,
    Set<X509Certificate> trustedCertificates
  ) throws Exception {
    // inspiration:
    // http://stackoverflow.com/a/26702631/535646
    // http://stackoverflow.com/a/9261365/535646
    // log.debug("getting signed data from byte array");
    // CMSProcessable signedContent = new CMSProcessableByteArray(pdfPKCS7.);
    // CMSSignedData signedData = null;
    // try {
    //   signedData = new CMSSignedData(signedContent, pdfPKCS7.getEncodedPKCS7());
    // } catch (CMSException e) {
    //   log.error("error getting signed data:" + e);
    //   return PdfSignatureValidationResult.builder().withError("Error de formato PKCS7").build();
    // }
    // log.debug("getting certificate store form signed data");
    // @SuppressWarnings("unchecked")
     Collection<X509CertificateHolder> holderCollection = new ArrayList<>();

        for (X509Certificate cert : (X509Certificate[]) pdfPKCS7.getCertificates()) {
            holderCollection.add(new X509CertificateHolder(cert.getEncoded()));
        }
    Store<X509CertificateHolder> certificatesStore = new JcaCertStore(holderCollection);
    // if (certificatesStore.getMatches(null).isEmpty()) {
    //   return PdfSignatureValidationResult.builder().withError("No contiene certificados").build();
    // }
    // log.debug("getting the signers");
    // Collection<SignerInformation> signers = signedData.getSignerInfos().getSigners();
    // if (signers.isEmpty()) {
    //   return PdfSignatureValidationResult.builder().withError("No especifica firmante").build();
    // }
    // log.debug("getting the signers information");
    // SignerInformation signerInformation = signers.iterator().next();
    // @SuppressWarnings("unchecked")
    // Collection<X509CertificateHolder> matches = certificatesStore.getMatches(
    //   signerInformation.getSID()
    // );
    // log.debug("matching the signers with the signer information");
    // if (matches.isEmpty()) {
    //   return PdfSignatureValidationResult
    //     .builder()
    //     .withError("No incluye el certificado del firmante")
    //     .build();
    // }
    // log.debug("getting the cert from signed data");
    // X509CertificateHolder certificateHolder = matches.iterator().next();
    // X509Certificate certFromSignedData = null;
    // try {
    //   certFromSignedData = new JcaX509CertificateConverter().getCertificate(certificateHolder);
    // } catch (CertificateException e) {
    //   return PdfSignatureValidationResult
    //     .builder()
    //     .withError("El certificado no está en formato X509")
    //     .build();
    // }
    X509Certificate certFromSignedData = pdfPKCS7.getSigningCertificate();

    log.debug("verify certificate usage");
    var result = new PdfSignatureValidationResult();
    try {
      var list = SigUtils.checkCertificateUsage(certFromSignedData);
      result.warnings.addAll(list);
    } catch (CertificateParsingException e) {
      result.warnings.add("El certificado no especifica uso extendido para su llave");
    }

    log.debug("verify timeStampToken");
    // Embedded timestamp
    TimeStampToken timeStampToken = null;
    try {
      timeStampToken = pdfPKCS7.getTimeStampToken();// SigUtils.extractTimeStampTokenFromSignerInformation(signerInformation);
    } catch (Exception e) {
      result.errors.add("El sello de tiempo contiene error de codificación");
    }
    if (timeStampToken != null) {
      boolean skipTimeStampTokenValidation = false;

      // tested with QV_RCA1_RCA3_CPCPS_V4_11.pdf
      // https://www.quovadisglobal.com/~/media/Files/Repository/QV_RCA1_RCA3_CPCPS_V4_11.ashx
      // also 021496.pdf and 036351.pdf from digitalcorpora
      try {
        SigUtils.validateTimestampToken(timeStampToken);
      } catch (IOException | CertificateException | TSPException | OperatorCreationException e) {
        result.errors.add("El token de sellado de tiempo es inválido");
        skipTimeStampTokenValidation = true;
      }

      if (!skipTimeStampTokenValidation) {
        @SuppressWarnings("unchecked") // TimeStampToken.getSID() is untyped
        Collection<X509CertificateHolder> tstMatches = timeStampToken
          .getCertificates()
          .getMatches((Selector<X509CertificateHolder>) timeStampToken.getSID());
        X509CertificateHolder tstCertHolder = tstMatches.iterator().next();
        X509Certificate certFromTimeStamp = null;
        try {
          certFromTimeStamp = new JcaX509CertificateConverter().getCertificate(tstCertHolder);
          result.infoMessages.add("TSA: " + certFromTimeStamp.getSubjectDN().getName());
        } catch (CertificateException e) {
          result.errors.add("El certificado del sello de tiempo no está en formato X509");
          skipTimeStampTokenValidation = true;
        }

        if (!skipTimeStampTokenValidation) {
          // merge both stores using a set to remove duplicates
          HashSet<X509CertificateHolder> certificateHolderSet = new HashSet<X509CertificateHolder>();
          List<X509CertificateHolder> holderList = new ArrayList<>();
          JcaX509CertificateConverter converter = new JcaX509CertificateConverter();

          for (X509Certificate cert : (X509Certificate[]) pdfPKCS7.getCertificates()) {
              holderList.add(new X509CertificateHolder(cert.getEncoded()));
          }
          // certificateHolderSet.addAll(certificatesStore.getMatches(null));
          certificateHolderSet.addAll(holderList);
          certificateHolderSet.addAll(timeStampToken.getCertificates().getMatches(null));
          try {
            SigUtils.verifyCertificateChain(
              new CollectionStore<X509CertificateHolder>(certificateHolderSet),
              certFromTimeStamp,
              trustedCertificates,
              timeStampToken.getTimeStampInfo().getGenTime()
            );
          } catch (CertificateRevokedException ex) {
            result.errors.add("El certificado del sello de tiempo es inválido porque fue revocado");
          } catch (CertificateRevocationException ex) {
            result.warnings.add(
              "El certificado del sello de tiempo es válido pero no se pudo comprobar si ha sido revocado"
            );
          } catch (CertificateVerificationException ex) {
            result.errors.add(ex.getMessage());
          } catch (Exception ex) {
            result.errors.add("El certificado del sello de tiempo es inválido");
          }
          try {
            var list = SigUtils.checkTimeStampCertificateUsage(certFromTimeStamp);
            result.warnings.addAll(list);
          } catch (CertificateParsingException e) {
            result.warnings.add(
              "El certificado del sello de tiempo no especifica uso extendido para su llave"
            );
          }

          try {
            // compare the hash of the signature with the hash in the timestamp
            byte[] tsMessageImprintDigest = timeStampToken
              .getTimeStampInfo()
              .getMessageImprintDigest();
            String hashAlgorithm = timeStampToken
              .getTimeStampInfo()
              .getMessageImprintAlgOID()
              .getId();
            byte[] sigMessageImprintDigest = MessageDigest
              .getInstance(hashAlgorithm)
              .digest(pdfPKCS7.getEncodedPKCS7());
            if (!Arrays.equals(tsMessageImprintDigest, sigMessageImprintDigest)) {
              System.err.println("timestamp signature verification failed");
              result.errors.add(
                "El sello de tiempo es inválido. La fecha de firma del PDF ha sido alterada."
              );
            }
          // } catch (NoSuchAlgorithmException ignored) {}
          } catch (Exception ignored) {}
        }
      }
    } else {
      result.warnings.add("La hora de la firma procede del dispositivo del firmante");
    }

    try {
      if (pdfPKCS7.getSignDate() != null) {
        certFromSignedData.checkValidity(pdfPKCS7.getSignDate().getTime());
      } else {
        result.errors.add("El certificado no especifica fecha de firma");
      }
    } catch (CertificateExpiredException ex) {
      result.errors.add("El certificado había expirado cuando se firmó el documento");
    } catch (CertificateNotYetValidException ex) {
      result.errors.add("El certificado aún no era válido cuando se firmó el documento");
    }

    // usually not available
    try {
      certFromSignedData.checkValidity(pdfPKCS7.getSignDate().getTime());
    } catch (CertificateExpiredException ex) {
      result.errors.add("El certificado había expirado cuando se firmó el documento");
    } catch (CertificateNotYetValidException ex) {
      result.errors.add("El certificado aún no era válido cuando se firmó el documento");
    }

    boolean isValid = pdfPKCS7.verify();
    if (!isValid) {
      result.errors.add("La firma es inválida. El archivo PDF ha sido modificado.");
    } else {
      result.infoMessages.add("El documento no ha sido modificado desde que se firmó");
    }

    boolean isSelfSigned = false;
    try {
      isSelfSigned = CertificateVerifier.isSelfSigned(certFromSignedData);
    } catch (GeneralSecurityException ignored) {}
    if (isSelfSigned) {
      result.errors.add("El certificado es autofirmado");
    } else {
      if (pdfPKCS7.getSignDate() != null) {
        try {
          SigUtils
            .getCertificateChain(certificatesStore, certFromSignedData, true)
            .forEach(cert -> {
              // System.out.println(cert.getSubjectX500Principal().getName());
              result.caChain.add(cert.getSubjectX500Principal().getName());
            });

          SigUtils.verifyCertificateChain(
            certificatesStore,
            certFromSignedData,
            trustedCertificates,
            pdfPKCS7.getSignDate().getTime()
          );
        } catch (CertificateRevokedException ex) {
          log.debug("El certificado es inválido porque fue revocado");
          result.errors.add("El certificado es inválido porque fue revocado");
        } catch (CertificateRevocationException ex) {
          log.debug("El certificado es válido pero no se pudo comprobar si ha sido revocado");
          result.warnings.add(
            "El certificado es válido pero no se pudo comprobar si ha sido revocado"
          );
        } catch (CertificateVerificationException ex) {
          log.debug("Error en certificate verification exception" + ex.getMessage());
          result.errors.add(ex.getMessage());
        } catch (Exception ex) {
          log.debug("Error no conocido: " + ex.getMessage());
          result.errors.add("El certificado es inválido");
        }
      }
    }

    return result;
  }
}
