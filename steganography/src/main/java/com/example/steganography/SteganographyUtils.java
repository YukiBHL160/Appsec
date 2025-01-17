package com.example.steganography;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;

public class SteganographyUtils {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";
    private static final String DELIMITER = "00000000"; // Example delimiter in binary

    // Method to encode a message into an image
    public static BufferedImage encode(File imageFile, String message, String secretKey) throws Exception {
        BufferedImage image = ImageIO.read(imageFile);

        // Encrypt the message
        String encryptedMessage = encrypt(message, secretKey);

        // Convert the encrypted message to binary and add a delimiter
        String binaryMessage = stringToBinary(encryptedMessage) + DELIMITER;

        // Embed the binary message into the image
        int messageLength = binaryMessage.length();
        int width = image.getWidth();
        int height = image.getHeight();
        int index = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (index < messageLength) {
                    int pixel = image.getRGB(x, y);
                    int newPixel = setLeastSignificantBit(pixel, binaryMessage.charAt(index));
                    image.setRGB(x, y, newPixel);
                    index++;
                }
            }
        }

        return image;
    }

    // Method to decode a message from an image
    public static String decode(File imageFile, String secretKey) throws Exception {
        BufferedImage image = ImageIO.read(imageFile);

        // Extract the binary message from the image
        StringBuilder binaryMessage = new StringBuilder();
        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                binaryMessage.append(getLeastSignificantBit(pixel));
            }
        }

        // Find the delimiter to extract the actual message
        String binaryString = binaryMessage.toString();
        int delimiterIndex = binaryString.indexOf(DELIMITER);
        if (delimiterIndex == -1) {
            throw new IllegalArgumentException("Delimiter not found in binary string.");
        }

        // Convert the binary message to a string
        String encryptedMessage = binaryToString(binaryString.substring(0, delimiterIndex));

        // Decrypt the message
        return decrypt(encryptedMessage, secretKey);
    }

    // Encrypt a message using AES
    private static String encrypt(String message, String secretKey) throws Exception {
        Key key = generateKey(secretKey);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // Decrypt a message using AES
    private static String decrypt(String encryptedMessage, String secretKey) throws Exception {
        Key key = generateKey(secretKey);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    // Generate a key from the secret key string
    private static Key generateKey(String secretKey) {
        return new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
    }

    // Convert a string to its binary representation
    private static String stringToBinary(String message) {
        StringBuilder binary = new StringBuilder();
        for (char c : message.toCharArray()) {
            binary.append(String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0'));
        }
        return binary.toString();
    }

    // Convert a binary string back to a regular string
    private static String binaryToString(String binary) {
        StringBuilder message = new StringBuilder();
        for (int i = 0; i < binary.length(); i += 8) {
            String byteString = binary.substring(i, i + 8);
            int charCode = Integer.parseInt(byteString, 2);
            message.append((char) charCode);
        }
        return message.toString();
    }

    // Set the least significant bit of a pixel to a binary value
    private static int setLeastSignificantBit(int pixel, char bit) {
        return (pixel & 0xFFFFFFFE) | (bit == '1' ? 1 : 0);
    }

    // Get the least significant bit of a pixel
    private static int getLeastSignificantBit(int pixel) {
        return pixel & 1;
    }
}
