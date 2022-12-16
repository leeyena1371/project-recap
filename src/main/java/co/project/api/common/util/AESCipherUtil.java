package co.project.api.common.util;


import org.springframework.security.crypto.codec.Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AESCipherUtil {

    private static final String initVector = String.format("%16s", ' ').replace(' ', '\0');
    private static final String ALGORITHM = "AES/CBC/PKCS5PADDING";

    private String key = null;

    public AESCipherUtil(String key) {
        this.key = key;
    }

    public String encryptToBase64(String plainText) throws Exception {
        return Base64.getEncoder().encodeToString(this.encrypt(plainText.getBytes(StandardCharsets.UTF_8)));
    }

    public String encryptToHex(String plainText) throws Exception {
        return new String(Hex.encode(this.encrypt(plainText.getBytes(StandardCharsets.UTF_8))));
    }

    public String decryptFromBase64(String cipherText) throws Exception {
        return new String(decrypt(Base64.getDecoder().decode(cipherText)), StandardCharsets.UTF_8);
    }

    public String decryptFromHex(String cipherText) throws Exception {
        return new String(decrypt(Hex.decode(cipherText)), StandardCharsets.UTF_8);
    }

    private byte[] encrypt(byte[] plainText) throws Exception {
        IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

        return cipher.doFinal(plainText);
    }

    private byte[] decrypt(byte[] cipherText) throws Exception {
        IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

        return cipher.doFinal(cipherText);
    }
}
