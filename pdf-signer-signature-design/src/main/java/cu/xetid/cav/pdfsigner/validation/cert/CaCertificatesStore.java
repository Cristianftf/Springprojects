package cu.xetid.cav.pdfsigner.validation.cert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.InterruptedException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ResourceUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Slf4j
public class CaCertificatesStore {
  private Set<X509Certificate> defaultTrustedCaCertificates;

  private CaCertificatesStore() {}

  public static CaCertificatesStore getInstance() {
    return InstanceHolder.instance;
  }

  public void setDefaultTrustedCertificates()
    throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, Exception {
    log.debug("fetching default trusted ca certicifaces");
    X509Certificate[] certificatesArray = this.fetchDefaultTrustedCaCertificates();
    log.debug("populating the default trusted ca certs");
    Set<X509Certificate> certificatesSet = new HashSet<>(Arrays.asList(certificatesArray));
    this.defaultTrustedCaCertificates = certificatesSet;
  }

  public synchronized Set<X509Certificate> getDefaultTrustedCaCertificates()
    throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, Exception {
    log.debug("getting default trusted certs");
    if (this.defaultTrustedCaCertificates == null) {
      log.debug("no default trusted certs found");
      this.setDefaultTrustedCertificates();
    }
    log.debug("returning the defalut trusted certs");
    return this.defaultTrustedCaCertificates;
  }

  private X509Certificate getCertificateFromMinio(String bucket, String key)
    throws IOException, CertificateException, InterruptedException, Exception {
    HttpClient httpClient = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest
      .newBuilder()
      .uri(URI.create("http://localhost:9000/" + bucket + "/" + key))
      .build();
    log.debug("getting InputStream from minio");
    HttpResponse<InputStream> response = httpClient.send(
      request,
      HttpResponse.BodyHandlers.ofInputStream()
    );
    int statusCode = response.statusCode();
    if (statusCode != 200) {
      throw new Exception("minio server response with status code: " + statusCode);
    }
    log.debug("generating cert from InputStream");
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    return (X509Certificate) cf.generateCertificate(response.body());
  }

  private Supplier<X509Certificate> createSupplier(String bucket, String key)
    throws IOException, CertificateException, InterruptedException, Exception {
    X509Certificate cert = this.getCertificateFromMinio(bucket, key);
    return () -> {
      return cert;
    };
  }

  public X509Certificate[] fetchTrustedCertificates(String bucket) throws Exception {
    return fetchTrustedCertificates(bucket, "@");
  }

  public X509Certificate[] fetchTrustedCertificates(String bucket, String prefix) throws Exception {
    try {
      log.debug("creating the http client");
      HttpClient httpClient = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest
        .newBuilder()
        .uri(
          URI.create("http://localhost:9000/" + bucket + (prefix != "@" ? "?prefix=" + prefix : ""))
        )
        .build();
      log.debug("making the http petition");
      HttpResponse<String> response = httpClient.send(
        request,
        HttpResponse.BodyHandlers.ofString()
      );
      int statusCode = response.statusCode();
      log.debug("http response with status code: " + statusCode);
      if (statusCode != 200) {
        throw new Exception("minio server response with status code : " + statusCode);
      }

      // Crear un objeto DocumentBuilderFactory para crear un objeto DocumentBuilder
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();

      log.debug("parsing the xml");
      // Crear un objeto Document a partir del String XML
      Document document = builder.parse(new InputSource(new StringReader(response.body())));

      // Obtener una lista de nodos de libro
      NodeList bookNodes = document.getElementsByTagName("Contents");
      CompletableFuture<X509Certificate>[] promises = new CompletableFuture[bookNodes.getLength()];

      log.debug("obtaining the " + bookNodes.getLength() + " bookNodes");
      // Iterar a través de la lista de nodos de libro y obtener los títulos y autores
      for (int i = 0; i < bookNodes.getLength(); i++) {
        Element bookElement = (Element) bookNodes.item(i);
        String key = bookElement.getElementsByTagName("Key").item(0).getTextContent();

        CompletableFuture<X509Certificate> promise = CompletableFuture.supplyAsync(
          this.createSupplier(bucket, key)
        );
        promises[i] = promise;
      }
      CompletableFuture
        .allOf(promises)
        .thenRun(() -> {
          log.debug("promesas ejecutadas con exito");
        })
        .join();
      X509Certificate[] acs = Arrays
        .stream(promises)
        .map(promise -> promise.join())
        .toArray(X509Certificate[]::new);
      return acs;
    } catch (IOException e) {
      log.error("Error getting from minio: " + e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Exception: " + e.getMessage());
      throw e;
    }
  }

  // TODO: poner esto bien y con variables de env
  private X509Certificate[] fetchDefaultTrustedCaCertificates() throws Exception {
    return fetchTrustedCertificates("default-trusted-certificates");
  }

  private static class InstanceHolder {

    private static final CaCertificatesStore instance = new CaCertificatesStore();
  }
}
