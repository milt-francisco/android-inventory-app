package com.cs360.inventory;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtil {
    public static final int ITERATIONS = 10000;
    public static final int KEY_LENGTH = 256;

    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    // Hash the password with the provided salt
    public static byte[] hashPassword(final char[] password, final byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return secretKeyFactory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verifyPassword(final char[] inputPassword, final byte[] salt, final byte[] storedHash) {
        byte[] inputHash = hashPassword(inputPassword, salt);
        return Arrays.equals(inputHash, storedHash);
    }
}
