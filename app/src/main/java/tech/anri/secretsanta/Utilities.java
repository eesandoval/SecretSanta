package tech.anri.secretsanta;

import android.util.Base64;

import java.security.SecureRandom;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Created by Rayziken on 10/20/2017.
 */

public final class Utilities {

    private static final int saltLength = 32;
    private static final int desiredKeyLength = 256;
    private static final int iterations = 20 * 1000;

    public static String getSaltedHash(String password) throws Exception {
        byte[] salt = SecureRandom.getInstance("SHA1PRNG").generateSeed(saltLength);
        return Base64.encodeToString(salt, Base64.DEFAULT) + "$" + createHash(password, salt);
    }

    public static boolean checkPassword(String password, String stored) throws Exception {
        String[] saltAndPass = stored.split("\\$");
        String hashOfInput = createHash(password, Base64.decode(saltAndPass[0], Base64.DEFAULT));
        return hashOfInput.equals(saltAndPass[1]);
    }

    private static String createHash(String password, byte[] salt) throws Exception {
        if (password == null || password.length() == 0)
            throw new IllegalArgumentException("Empty password");
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        SecretKey secretKey = f.generateSecret(new PBEKeySpec(password.toCharArray(), salt, iterations, desiredKeyLength));
        return Base64.encodeToString(secretKey.getEncoded(), Base64.DEFAULT);
    }
}
