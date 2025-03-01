package socialMediaApp.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class SteganographyUtil {

    private static final int HEADER_SIZE = 32; // 32 bits to store data length

    // Generates a random AES key for encryption
    public static SecretKey generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128, new SecureRandom());
        return keyGenerator.generateKey();
    }

    // Encrypts data using AES
    private static byte[] encrypt(String data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    // Decrypts data using AES
    private static String decrypt(byte[] encryptedData, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(encryptedData), StandardCharsets.UTF_8);
    }

    // Hides encrypted data inside an image
    public static byte[] hideData(byte[] imageData, String data, SecretKey key) throws Exception {
        byte[] encryptedData = encrypt(data, key);
        int dataLength = encryptedData.length;
        int totalBitsNeeded = HEADER_SIZE + dataLength * 8;

        if (totalBitsNeeded > imageData.length) {
            throw new IllegalArgumentException("Image too small to store hidden data.");
        }

        byte[] output = imageData.clone();

        // Store data length in first 32 bits
        for (int i = 0; i < HEADER_SIZE; i++) {
            int bit = (dataLength >> (31 - i)) & 1;
            output[i] = (byte) ((output[i] & 0xFE) | bit);
        }

        // Store encrypted data
        for (int i = 0; i < dataLength; i++) {
            for (int bitIndex = 0; bitIndex < 8; bitIndex++) {
                int bit = (encryptedData[i] >> (7 - bitIndex)) & 1;
                int pos = HEADER_SIZE + i * 8 + bitIndex;
                output[pos] = (byte) ((output[pos] & 0xFE) | bit);
            }
        }

        return output;
    }

    // Extracts and decrypts hidden data from an image
    public static String extractData(byte[] imageData, SecretKey key) throws Exception {
        if (imageData.length < HEADER_SIZE) {
            return "null";
        }

        // Read hidden data length
        int dataLength = 0;
        for (int i = 0; i < HEADER_SIZE; i++) {
            int bit = imageData[i] & 1;
            dataLength = (dataLength << 1) | bit;
        }

        int totalBitsNeeded = HEADER_SIZE + dataLength * 8;
        if (totalBitsNeeded > imageData.length) {
            throw new IllegalArgumentException("Image data does not contain enough bits.");
        }

        byte[] encryptedData = new byte[dataLength];

        // Extract hidden data bits
        for (int i = 0; i < dataLength; i++) {
            int b = 0;
            for (int bitIndex = 0; bitIndex < 8; bitIndex++) {
                int pos = HEADER_SIZE + i * 8 + bitIndex;
                int bit = imageData[pos] & 1;
                b = (b << 1) | bit;
            }
            encryptedData[i] = (byte) b;
        }

        // Decrypt the extracted data
        return decrypt(encryptedData, key);
    }

    // Checks if the image contains steganographed data
    public static boolean isImageSteganographed(byte[] imageData, SecretKey key) {
        try {
            String extractedData = extractData(imageData, key);
            // Check if the extracted data is valid (e.g., contains an email and a timestamp)
            if (extractedData != null && !extractedData.equals("null")) {
                // Example: Check if the extracted data contains a valid email and timestamp
                String[] parts = extractedData.split("\\|");
                if (parts.length == 2) {
                    String email = parts[0];
                    long timestamp = Long.parseLong(parts[1]);
                    // Basic validation for email and timestamp
                    if (email.contains("@") && timestamp > 0) {  //data :  aaaaaaa \\ donnnes dimages
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            // If extraction fails, the image is not steganographed
            return false;
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        SecretKey key = generateKey();
        String secretMessage = "user@example.com|1234567890"; // Example: email|timestamp
        byte[] imageData = new byte[1000]; // Simulated image bytes

        // Check if the image is steganographed (should return false initially)
        boolean isSteganographed = isImageSteganographed(imageData, key);
        System.out.println("Is image steganographed? " + isSteganographed);

        // Hide data in the image
        byte[] stegoImage = hideData(imageData, secretMessage, key);

        // Check again if the image is steganographed (should return true)
        isSteganographed = isImageSteganographed(stegoImage, key);
        System.out.println("Is image steganographed? " + isSteganographed);

        // Extract the hidden message
        String extractedMessage = extractData(stegoImage, key);
        System.out.println("Extracted Message: " + extractedMessage);
    }
}