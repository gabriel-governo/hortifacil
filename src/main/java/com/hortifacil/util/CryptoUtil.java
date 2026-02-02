package com.hortifacil.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.util.Base64;
import java.security.SecureRandom;

public class CryptoUtil {

    private static final String ALGO = "AES";
    private static final int KEY_SIZE = 128;
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_SIZE = 12; // GCM recomendação
    private static SecretKey secretKey;

    static {
        try {
            // Para produção, você deve gerar a chave uma vez e armazenar em lugar seguro
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGO);
            keyGen.init(KEY_SIZE);
            secretKey = keyGen.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String encrypt(String valor) throws Exception {
        byte[] iv = new byte[IV_SIZE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
        byte[] encrypted = cipher.doFinal(valor.getBytes());
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decrypt(String encryptedBase64) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedBase64);
        byte[] iv = new byte[IV_SIZE];
        byte[] encrypted = new byte[combined.length - IV_SIZE];
        System.arraycopy(combined, 0, iv, 0, IV_SIZE);
        System.arraycopy(combined, IV_SIZE, encrypted, 0, encrypted.length);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted);
    }
}
