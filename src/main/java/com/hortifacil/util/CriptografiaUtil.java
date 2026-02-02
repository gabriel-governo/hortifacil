package com.hortifacil.util;

import org.mindrot.jbcrypt.BCrypt;

public class CriptografiaUtil {

    // Gera hash
    public static String criptografar(String texto) {
        return BCrypt.hashpw(texto, BCrypt.gensalt());
    }

    // Verifica correspondência
    public static boolean verificar(String texto, String hash) {
        return BCrypt.checkpw(texto, hash);
    }
}
