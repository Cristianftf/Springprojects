package com.PrototipoManageService.PetuniaPrototipeSpring.util;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class EncryptionUtil {

    @Value("${encryption.secretKey}")
    private String secretKey;

    @Value("${encryption.initVector}")
    private String initVector;

    private SecretKeySpec secretKeySpec;
    private IvParameterSpec ivParameterSpec;
    private final String ALGORITHM = "AES/CBC/PKCS5Padding";

    @PostConstruct
    public void init() {
        try {
            if (secretKey.length() != 16 || initVector.length() != 16) {
                throw new IllegalArgumentException("La clave secreta y el vector de inicialización deben tener 16 caracteres cada uno.");
            }
            secretKeySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), "AES");
            ivParameterSpec = new IvParameterSpec(initVector.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException("Error al inicializar EncryptionUtil", e);
        }
    }

    public String encrypt(String value) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] encrypted = cipher.doFinal(value.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            throw new RuntimeException("Error encriptando", ex);
        }
    }

    public String decrypt(String encryptedValue) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] decoded = Base64.getDecoder().decode(encryptedValue);
            byte[] original = cipher.doFinal(decoded);
            return new String(original, "UTF-8");
        } catch (Exception ex) {
            throw new RuntimeException("Error desencriptando", ex);
        }
    }
}