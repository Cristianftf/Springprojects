package cu.xetid.cav.pdfsigner;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PdfsignerApplication {

  public static void main(String[] args) {
    // TODO: quitar esto, es solo para desarrollo
    String password = "thenecesaryROZENDO";
    System.setProperty("http.proxyHost", "10.12.0.205");
    System.setProperty("http.proxyPort", "3128");
    System.setProperty("http.proxyUser", "zequeira");
    System.setProperty("http.proxyPassword", "thenecesaryROZENDO");
    System.setProperty("http.nonProxyHosts", "*.xetid.cu");

    Authenticator.setDefault(
      new Authenticator() {
        public PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication("zequeira", password.toCharArray());
        }
      }
    );

    SpringApplication.run(PdfsignerApplication.class, args);
  }
}
// curl -X 'POST' 'http://localhost:8080' -H 'accept: application/json' -H 'Content-Type: multipart/form-data' -F 'pkcs12=@/home/charlie/Downloads/certificado.p12;type=application/x-pkcs12' -F 'pdf=@/home/charlie/Downloads/a.pdf;type=application/pdf' -F 'options=@/home/charlie/Downloads/pass.txt;type=application/json' --output 'firmado.pdf'
