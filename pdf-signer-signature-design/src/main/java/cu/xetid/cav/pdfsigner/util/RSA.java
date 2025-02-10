package cu.xetid.cav.pdfsigner.util;

// Importaciones necesarias para el funcionamiento de la clase
import java.io.UnsupportedEncodingException;
import java.io.UnsupportedEncodingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import lombok.extern.slf4j.Slf4j;

// Anotación para habilitar el registro de logs
@Slf4j
public class RSA {

  // Variables para almacenar las claves privada y pública
  private PrivateKey privateKey;
  private PublicKey publicKey;

  // Constructor privado para el patrón Singleton
  private RSA() {}

  // Método para obtener la instancia única de la clase
  public static RSA getInstance() {
    return InstanceHolder.instance;
  }

  // Método para obtener la clave pública codificada en Base64
  public String getPublicKey() throws NoSuchAlgorithmException {
    if (this.publicKey == null) {
      log.debug("no hay clave pública");
      this.generateKeyPair();
    }
    return Base64.getEncoder().encodeToString(this.publicKey.getEncoded());
  }

  // Método para generar un par de claves RSA
  public void generateKeyPair() throws NoSuchAlgorithmException {
    // Crear una instancia de KeyPairGenerator para RSA
    log.debug("generando par de claves");
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");

    // Configurar el tamaño de la clave a 2048 bits
    keyGen.initialize(2048);

    // Generar un par de claves RSA
    KeyPair keyPair = keyGen.generateKeyPair();

    // Guardar la clave pública y privada generadas
    this.privateKey = keyPair.getPrivate();
    this.publicKey = keyPair.getPublic();
    log.debug("par de claves generado");
  }

  // Método para descifrar un mensaje usando RSA
  public String decryptMessage(String encryptedMessage) throws Exception {
    try {
      if (this.publicKey == null) {
        log.debug("no hay clave pública");
        this.generateKeyPair();
        throw new Exception("No se habian definido las llaves");
      }
      // // Cifrar un mensaje con la clave pública
      //         Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
      //         cipher.init(Cipher.ENCRYPT_MODE, this.publicKey);
      //         String message = "!QAZxsw2";
      //         byte[] encryptedBytes = cipher.doFinal(message.getBytes());
      //         String encrypted = Base64.getEncoder().encodeToString(encryptedBytes);
      //         System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
      //         System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
      //         System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
      //         System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
      //         System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
      //         System.out.println(encrypted);

      // Descifrar el mensaje cifrado con la clave privada
      Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
      cipher.init(Cipher.DECRYPT_MODE, this.privateKey);
      log.debug("Decodificando la contraseña");
      byte[] encriptedMessageBytes = Base64.getDecoder().decode(encryptedMessage);
      log.debug("Descifrando la contraseña");
      byte[] decryptedMessageBytes = cipher.doFinal(encriptedMessageBytes);

      // Convertir los bytes descifrados a String
      String decryptedMessage = new String(decryptedMessageBytes);

      return decryptedMessage;
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new Exception(e.getMessage());
    }
  }

  // Clase interna para implementar el patrón Singleton
  private static class InstanceHolder {
    private static final RSA instance = new RSA();
  }
}
