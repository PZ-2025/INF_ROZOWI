package pl.rozowi.app.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordChangeService {

    public String validateAndHashPassword(String newPassword, String confirmPassword) throws IllegalArgumentException {
        if (newPassword == null || confirmPassword == null ||
                newPassword.trim().isEmpty() || confirmPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Pole hasła nie może być puste!");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Hasła nie są takie same!");
        }

        return hashPassword(newPassword);
    }

    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not available", e);
        }
    }
}