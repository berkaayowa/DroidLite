package co.za.onebyte.hhtlibrary.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;

/**
 * AES encryption utility for NTAG424 DNA
 */
public class AesUtil {

    /**
     * Encrypt data using AES-128 in CBC mode
     * @param key AES key (16 bytes)
     * @param data Data to encrypt
     * @return Encrypted data with IV prepended
     */
    public static byte[] encrypt(byte[] key, byte[] data) {
        try {
            // Generate random IV
            SecureRandom random = new SecureRandom();
            byte[] iv = new byte[16];
            random.nextBytes(iv);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            // Pad data to multiple of 16 bytes
            byte[] paddedData = padData(data);

            // Encrypt
            byte[] encrypted = cipher.doFinal(paddedData);

            // Prepend IV to encrypted data
            byte[] result = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Decrypt data using AES-128 in CBC mode
     * @param key AES key (16 bytes)
     * @param encryptedData Encrypted data with IV prepended
     * @return Decrypted data
     */
    public static byte[] decrypt(byte[] key, byte[] encryptedData) {
        try {
            // Extract IV (first 16 bytes)
            byte[] iv = new byte[16];
            System.arraycopy(encryptedData, 0, iv, 0, iv.length);

            // Extract encrypted data (remaining bytes)
            byte[] data = new byte[encryptedData.length - iv.length];
            System.arraycopy(encryptedData, iv.length, data, 0, data.length);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            // Decrypt
            byte[] decrypted = cipher.doFinal(data);

            // Remove padding
            return unpadData(decrypted);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Pad data to multiple of 16 bytes using ISO 7816-4 padding
     */
    private static byte[] padData(byte[] data) {
        int paddingLength = 16 - (data.length % 16);
        if (paddingLength == 0) paddingLength = 16;

        byte[] padded = new byte[data.length + paddingLength];
        System.arraycopy(data, 0, padded, 0, data.length);
        padded[data.length] = (byte) 0x80; // Start padding with 0x80

        for (int i = data.length + 1; i < padded.length; i++) {
            padded[i] = 0x00; // Fill rest with zeros
        }

        return padded;
    }

    /**
     * Remove ISO 7816-4 padding from data
     */
    private static byte[] unpadData(byte[] data) {
        int i = data.length - 1;
        while (i >= 0 && data[i] == 0x00) {
            i--;
        }

        if (i >= 0 && data[i] == (byte) 0x80) {
            byte[] unpadded = new byte[i];
            System.arraycopy(data, 0, unpadded, 0, i);
            return unpadded;
        }

        return data; // No padding found
    }

    /**
     * Generate CMAC for authentication (simplified version)
     */
    public static byte[] generateCmac(byte[] key, byte[] data) {
        // This is a simplified implementation
        // For production, use a proper CMAC implementation
        try {
            // For NTAG424, you would typically use a proper CMAC algorithm
            // This is a placeholder - you should implement proper CMAC
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            // Pad data to multiple of 16 bytes
            byte[] paddedData = padData(data);

            return cipher.doFinal(paddedData);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}