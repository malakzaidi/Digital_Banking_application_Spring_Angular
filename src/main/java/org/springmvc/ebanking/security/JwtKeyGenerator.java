package org.springmvc.ebanking.security;

import java.security.SecureRandom;
import java.util.Base64;

public class JwtKeyGenerator {
    public static void main(String[] args) {
        byte[] secretKey = new byte[48];
        new SecureRandom().nextBytes(secretKey);
        String encodedKey = Base64.getEncoder().encodeToString(secretKey);
        System.out.println("JWT Secret Key (Base64): " + encodedKey);
    }
}