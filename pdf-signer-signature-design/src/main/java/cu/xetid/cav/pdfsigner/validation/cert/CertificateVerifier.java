package cu.xetid.cav.pdfsigner.validation.cert;

import cu.xetid.cav.pdfsigner.pdfsigner.signature.SigUtils;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.encryption.SecurityProvider;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPResp;

/**
 * Copied from Apache CXF 2.4.9, initial version:
 * https://svn.apache.org/repos/asf/cxf/tags/cxf-2.4.9/distribution/src/main/release/samples/sts_issue_operation/src/main/java/demo/sts/provider/cert/
 */
@Slf4j
public final class CertificateVerifier {

  // private static final Log LOG = LogFactory.getLog(CertificateVerifier.class);

  private CertificateVerifier() {}

  /**
   * Attempts to build a certification chain for given certificate and to
   * verify it. Relies on a set of root CA certificates and intermediate
   * certificates that will be used for building the certification chain. The
   * verification process assumes that all self-signed certificates in the set
   * are trusted root CA certificates and all other certificates in the set
   * are intermediate certificates.
   *
   * @param cert                 - certificate for validation
   * @param additionalCerts      - set of trusted root CA certificates that will be
   *                             used as "trust anchors" and intermediate CA certificates that will be
   *                             used as part of the certification chain. All self-signed certificates are
   *                             considered to be trusted root CA certificates. All the rest are
   *                             considered to be intermediate CA certificates.
   * @param verifySelfSignedCert true if a self-signed certificate is accepted, false if not.
   * @param signDate             the date when the signing took place
   * @return the certification chain (if verification is successful)
   * @throws CertificateVerificationException - if the certification is not
   *                                          successful (e.g. certification path cannot be built or some certificate
   *                                          in the chain is expired or CRL checks are failed)
   */
  public static PKIXCertPathBuilderResult verifyCertificate(
    X509Certificate cert,
    Set<X509Certificate> additionalCerts,
    Set<X509Certificate> trustedCertificates,
    boolean verifySelfSignedCert,
    Date signDate
  ) throws GeneralSecurityException, CertificateVerificationException, RevokedCertificateException {
    // Check for self-signed certificate
    if (!verifySelfSignedCert && isSelfSigned(cert)) {
      throw new CertificateVerificationException("El certificado es autofirmado");
    }

    // Set<X509Certificate> certSet = new HashSet<X509Certificate>(additionalCerts);
    Set<X509Certificate> certSet = new HashSet<X509Certificate>();

    // Download extra certificates. However, each downloaded certificate can lead to
    // more extra certificates, e.g. with the file from PDFBOX-4091, which has
    // an incomplete chain.
    // You can skip this block if you know that the certificate chain is complete
    Set<X509Certificate> certsToTrySet = new HashSet<X509Certificate>();
    certsToTrySet.add(cert);
    certsToTrySet.addAll(additionalCerts);
    int downloadSize = 0;
    while (!certsToTrySet.isEmpty()) {
      Set<X509Certificate> nextCertsToTrySet = new HashSet<X509Certificate>();
      for (X509Certificate tryCert : certsToTrySet) {
        certSet.add(tryCert);
        Set<X509Certificate> downloadedExtraCertificatesSet = CertificateVerifier.downloadExtraCertificates(
          tryCert
        );
        log.debug("downloaded: " + downloadedExtraCertificatesSet.size());
        for (X509Certificate downloadedCertificate : downloadedExtraCertificatesSet) {
          if (!certSet.contains(downloadedCertificate)) {
            nextCertsToTrySet.add(downloadedCertificate);
            certSet.add(downloadedCertificate);
            downloadSize++;
          }
        }
        if (trustedCertificates.contains(tryCert)) break;
      }
      certsToTrySet = nextCertsToTrySet;
    }
    if (downloadSize > 0) {
      log.info("CA issuers: " + downloadSize + " downloaded certificate(s) are new");
    }

    // Prepare a set of trust anchors (set of root CA certificates)
    // and a set of intermediate certificates
    Set<X509Certificate> intermediateCerts = new HashSet<X509Certificate>();
    Set<TrustAnchor> trustAnchors = new HashSet<TrustAnchor>();

    log.debug("aditionalsCert: " + additionalCerts.size());
    for (X509Certificate additionalCert : certSet) {
      log.debug("aditional cert: " + additionalCert.getSubjectDN());
      if (trustedCertificates.contains(additionalCert)) {
        trustAnchors.add(new TrustAnchor(additionalCert, null));
      } else {
        intermediateCerts.add(additionalCert);
      }
    }

    if (trustAnchors.isEmpty()) {
      throw new CertificateVerificationException(
        "El certificado es inválido. Fue creado por una Entidad Certificadora (CA) desconocida."
      );
    }

    // Attempt to build the certification chain and verify it
    PKIXCertPathBuilderResult verifiedCertChain = verifyCertificate(
      cert,
      trustAnchors,
      intermediateCerts,
      signDate
    );

    log.info(
      "Certification chain verified successfully up to this root: " +
      verifiedCertChain.getTrustAnchor().getTrustedCert().getSubjectX500Principal()
    );

    checkRevocations(cert, certSet, trustedCertificates, signDate);

    return verifiedCertChain;
  }

  private static void checkRevocations(
    X509Certificate cert,
    Set<X509Certificate> additionalCerts,
    Set<X509Certificate> trustedCertificates,
    Date signDate
  )
    throws CertificateException, NoSuchAlgorithmException, NoSuchProviderException, RevokedCertificateException, CertificateVerificationException {
    if (isSelfSigned(cert)) {
      // root, we're done
      log.debug("root, we're done");
      return;
    }
    for (X509Certificate additionalCert : additionalCerts) {
      try {
        log.debug("verify additional cert: " + additionalCert.getSubjectDN());
        cert.verify(additionalCert.getPublicKey(), SecurityProvider.getProvider().getName());
        checkRevocationsWithIssuer(
          cert,
          additionalCert,
          additionalCerts,
          trustedCertificates,
          signDate
        );
        // there can be several issuers
      } catch (GeneralSecurityException | IOException ex) {
        log.debug("Error in verify revocations, cert is not the user: " + ex.getMessage());
        // not the issuer
      }
    }
  }

  private static void checkRevocationsWithIssuer(
    X509Certificate cert,
    X509Certificate issuerCert,
    Set<X509Certificate> additionalCerts,
    Set<X509Certificate> trustedCertificates,
    Date signDate
  ) throws RevokedCertificateException, CertificateVerificationException, GeneralSecurityException {
    // Try checking the certificate through OCSP (faster than CRL)
    boolean verifyByCRLs = true;
    String ocspURL = extractOCSPURL(cert);
    if (ocspURL != null) {
      OcspHelper ocspHelper = new OcspHelper(cert, signDate, issuerCert, additionalCerts, ocspURL);
      try {
        verifyOCSP(ocspHelper, additionalCerts, trustedCertificates);
        verifyByCRLs = false;
      } catch (IOException | OCSPException ex) {
        log.warn("Exception trying OCSP: " + ocspURL + " - " + ex.getMessage());
      }
    }
    if (verifyByCRLs) {
      log.info("OCSP not available, will try CRL");

      // Check whether the certificate is revoked by the CRL
      // given in its CRL distribution point extension
      CRLVerifier.verifyCertificateCRLs(cert, signDate, additionalCerts, trustedCertificates);
    }

    // now check the issuer
    checkRevocations(issuerCert, additionalCerts, trustedCertificates, signDate);
  }

  /**
   * Checks whether given X.509 certificate is self-signed.
   *
   * @param cert The X.509 certificate to check.
   * @return true if the certificate is self-signed, false if not.
   * @throws java.security.GeneralSecurityException
   */
  public static boolean isSelfSigned(X509Certificate cert)
    throws CertificateException, NoSuchAlgorithmException, NoSuchProviderException {
    try {
      // Try to verify certificate signature with its own public key
      log.debug("Verify if cert" + cert.getSubjectDN().getName() + " is self signed");
      PublicKey key = cert.getPublicKey();
      cert.verify(key, SecurityProvider.getProvider().getName());
      log.debug("Certificate is self signed");
      return true;
    } catch (SignatureException ex) {
      // Invalid signature --> not self-signed
      log.debug("Certificate is not self signed");
      return false;
    } catch (InvalidKeyException ex) {
      // Invalid signature --> not self-signed
      log.debug(
        "Couldn't get signature information - InvalidKeyException - returning false - " +
        ex.getMessage()
      );
      return false;
    } catch (IOException ex) {
      // Invalid signature --> not self-signed
      log.debug(
        "Couldn't get signature information - IOException - returning false - " + ex.getMessage()
      );
      return false;
    } catch (Exception e) {
      log.debug("Error verifing if cert is selfsigned: " + e.getMessage());
      return false;
    }
  }

  /**
   * Download extra certificates from the URI mentioned in id-ad-caIssuers in the "authority
   * information access" extension. The method is lenient, i.e. catches all exceptions.
   *
   * @param ext an X509 object that can have extensions.
   * @return a certificate set, never null.
   */
  public static Set<X509Certificate> downloadExtraCertificates(X509Extension ext) {
    // https://tools.ietf.org/html/rfc2459#section-4.2.2.1
    // https://tools.ietf.org/html/rfc3280#section-4.2.2.1
    // https://tools.ietf.org/html/rfc4325
    Set<X509Certificate> resultSet = new HashSet<X509Certificate>();
    byte[] authorityExtensionValue = ext.getExtensionValue(Extension.authorityInfoAccess.getId());
    if (authorityExtensionValue == null) {
      return resultSet;
    }
    ASN1Primitive asn1Prim;
    try {
      asn1Prim = JcaX509ExtensionUtils.parseExtensionValue(authorityExtensionValue);
    } catch (IOException ex) {
      log.warn("Error parsing extension value: " + ex.getMessage());
      return resultSet;
    }
    if (!(asn1Prim instanceof ASN1Sequence)) {
      log.warn("ASN1Sequence expected, got " + asn1Prim.getClass().getSimpleName());
      return resultSet;
    }
    ASN1Sequence asn1Seq = (ASN1Sequence) asn1Prim;
    Enumeration<?> objects = asn1Seq.getObjects();
    while (objects.hasMoreElements()) {
      // AccessDescription
      ASN1Sequence obj = (ASN1Sequence) objects.nextElement();
      ASN1Encodable oid = obj.getObjectAt(0);
      if (!X509ObjectIdentifiers.id_ad_caIssuers.equals(oid)) {
        continue;
      }
      ASN1TaggedObject location = (ASN1TaggedObject) obj.getObjectAt(1);
      ASN1OctetString uri = (ASN1OctetString) location.getObject();
      String urlString = new String(uri.getOctets());
      InputStream in = null;
      try {
        log.info("CA issuers URL: " + urlString);
        Collection<? extends Certificate> altCerts = SignatureValidationCache
          .getInstance()
          .caIssuersCertificates.getIfPresent(urlString);
        if (altCerts != null) {
          log.info("CA issuers URL: " + altCerts.size() + " certificate(s) loaded from cache");
        } else {
          in = SigUtils.openURL(urlString);
          CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
          altCerts = certFactory.generateCertificates(in);
          SignatureValidationCache.getInstance().caIssuersCertificates.put(urlString, altCerts);
          log.info("CA issuers URL: " + altCerts.size() + " certificate(s) downloaded");
        }

        for (Certificate altCert : altCerts) {
          resultSet.add((X509Certificate) altCert);
        }
      } catch (IOException ex) {
        log.warn(urlString + " failure: " + ex.getMessage());
      } catch (CertificateException ex) {
        log.warn(ex.getMessage());
      } finally {
        IOUtils.closeQuietly(in);
      }
    }
    log.info("CA issuers: Downloaded " + resultSet.size() + " certificate(s) total");
    return resultSet;
  }

  /**
   * Attempts to build a certification chain for given certificate and to
   * verify it. Relies on a set of root CA certificates (trust anchors) and a
   * set of intermediate certificates (to be used as part of the chain).
   *
   * @param cert              - certificate for validation
   * @param trustAnchors      - set of trust anchors
   * @param intermediateCerts - set of intermediate certificates
   * @param signDate          the date when the signing took place
   * @return the certification chain (if verification is successful)
   * @throws GeneralSecurityException - if the verification is not successful
   *                                  (e.g. certification path cannot be built or some certificate in the chain
   *                                  is expired)
   */
  private static PKIXCertPathBuilderResult verifyCertificate(
    X509Certificate cert,
    Set<TrustAnchor> trustAnchors,
    Set<X509Certificate> intermediateCerts,
    Date signDate
  ) throws GeneralSecurityException {
    // Create the selector that specifies the starting certificate
    X509CertSelector selector = new X509CertSelector();
    selector.setCertificate(cert);

    // Configure the PKIX certificate builder algorithm parameters
    PKIXBuilderParameters pkixParams = new PKIXBuilderParameters(trustAnchors, selector);

    // Disable CRL checks (this is done manually as additional step)
    pkixParams.setRevocationEnabled(false);

    // not doing this brings
    // "SunCertPathBuilderException: unable to find valid certification path to requested target"
    // (when using -Djava.security.debug=certpath: "critical policy qualifiers present in certificate")
    // for files like 021496.pdf that have the "Adobe CDS Certificate Policy" 1.2.840.113583.1.2.1
    // CDS = "Certified Document Services"
    // https://www.adobe.com/misc/pdfs/Adobe_CDS_CP.pdf
    pkixParams.setPolicyQualifiersRejected(false);
    // However, maybe there is still work to do:
    // "If the policyQualifiersRejected flag is set to false, it is up to the application
    // to validate all policy qualifiers in this manner in order to be PKIX compliant."

    pkixParams.setDate(signDate);

    // Specify a list of intermediate certificates
    CertStore intermediateCertStore = CertStore.getInstance(
      "Collection",
      new CollectionCertStoreParameters(intermediateCerts)
    );
    pkixParams.addCertStore(intermediateCertStore);

    // Build and verify the certification chain
    // If this doesn't work although it should, it can be debugged
    // by starting java with -Djava.security.debug=certpath
    // see also
    // https://docs.oracle.com/javase/8/docs/technotes/guides/security/troubleshooting-security.html
    CertPathBuilder builder = CertPathBuilder.getInstance("PKIX");
    return (PKIXCertPathBuilderResult) builder.build(pkixParams);
  }

  /**
   * Extract the OCSP URL from an X.509 certificate if available.
   *
   * @param cert X.509 certificate
   * @return the URL of the OCSP validation service
   * @throws IOException
   */
  private static String extractOCSPURL(X509Certificate cert) {
    try {
      byte[] authorityExtensionValue = cert.getExtensionValue(
        Extension.authorityInfoAccess.getId()
      );
      if (authorityExtensionValue != null) {
        // copied from CertInformationHelper.getAuthorityInfoExtensionValue()
        // DRY refactor should be done some day
        ASN1Sequence asn1Seq = (ASN1Sequence) JcaX509ExtensionUtils.parseExtensionValue(
          authorityExtensionValue
        );
        Enumeration<?> objects = asn1Seq.getObjects();
        while (objects.hasMoreElements()) {
          // AccessDescription
          ASN1Sequence obj = (ASN1Sequence) objects.nextElement();
          ASN1Encodable oid = obj.getObjectAt(0);
          // accessLocation
          ASN1TaggedObject location = (ASN1TaggedObject) obj.getObjectAt(1);
          if (
            X509ObjectIdentifiers.id_ad_ocsp.equals(oid) &&
            location.getTagNo() == GeneralName.uniformResourceIdentifier
          ) {
            ASN1OctetString url = (ASN1OctetString) location.getObject();
            String ocspURL = new String(url.getOctets());
            log.info("OCSP URL: " + ocspURL);
            return ocspURL;
          }
        }
      }
    } catch (IOException ignored) {}
    return null;
  }

  /**
   * Verify whether the certificate has been revoked at signing date, and verify whether the
   * certificate of the responder has been revoked now.
   *
   * @param ocspHelper      the OCSP helper.
   * @param additionalCerts
   * @throws RevokedCertificateException
   * @throws IOException
   * @throws OCSPException
   * @throws CertificateVerificationException
   */
  private static void verifyOCSP(
    OcspHelper ocspHelper,
    Set<X509Certificate> additionalCerts,
    Set<X509Certificate> trustedCertificates
  )
    throws IOException, RevokedCertificateException, OCSPException, CertificateVerificationException, GeneralSecurityException {
    OCSPResp ocspResponse = ocspHelper.getResponseOcsp();
    if (ocspResponse.getStatus() != OCSPResp.SUCCESSFUL) {
      log.info("OCSP check not successful");
      throw new OCSPException("OCSP check response not successfull");
    }
    log.info("OCSP check successful");

    BasicOCSPResp basicResponse = (BasicOCSPResp) ocspResponse.getResponseObject();
    X509Certificate ocspResponderCertificate = ocspHelper.getOcspResponderCertificate();
    byte[] ocspNoCheckExtensionValue = ocspResponderCertificate.getExtensionValue(
      OCSPObjectIdentifiers.id_pkix_ocsp_nocheck.getId()
    );
    if (ocspNoCheckExtensionValue != null) {
      // https://tools.ietf.org/html/rfc6960#section-4.2.2.2.1
      // A CA may specify that an OCSP client can trust a responder for the
      // lifetime of the responder's certificate.  The CA does so by
      // including the extension id-pkix-ocsp-nocheck.
      log.info(
        "Revocation check of OCSP responder certificate skipped (id-pkix-ocsp-nocheck is set)"
      );
      return;
    }

    if (ocspHelper.getCertificateToCheck().equals(ocspResponderCertificate)) {
      log.info("OCSP responder certificate is identical to certificate to check");
      return;
    }

    log.info("Check of OCSP responder certificate");
    Set<X509Certificate> additionalCerts2 = new HashSet<X509Certificate>(additionalCerts);
    JcaX509CertificateConverter certificateConverter = new JcaX509CertificateConverter();
    for (X509CertificateHolder certHolder : basicResponse.getCerts()) {
      try {
        X509Certificate cert = certificateConverter.getCertificate(certHolder);
        if (!ocspResponderCertificate.equals(cert)) {
          additionalCerts2.add(cert);
        }
      } catch (CertificateException ex) {
        // unlikely to happen because the certificate existed as an object
        log.error(ex.getMessage());
      }
    }
    Date now = Calendar.getInstance().getTime();
    CertificateVerifier.verifyCertificate(
      ocspResponderCertificate,
      additionalCerts2,
      trustedCertificates,
      true,
      now
    );
    log.info("Check of OCSP responder certificate done");
  }
}
