package co.za.onebyte.hhtlibrary.utility;

import java.nio.charset.Charset;

public class Str {

    /**
     * Convert hex string (e.g., "A0A1A2A3A4A5") to byte array
     * @param hexString The key as hex string
     * @return byte array or null if invalid
     */
    public static byte[] hexStringToByteArray(String hexString) {

        if (hexString == null)
            return null;

        hexString = hexString.replaceAll("\\s", ""); // remove spaces

        int len = hexString.length();
        if (len % 2 != 0) {
            // Hex string must have even length
            throw new IllegalArgumentException("Invalid hex string length");
        }

        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i+1), 16));
        }

        return data;
    }

    /** Convert byte array to hex string (e.g., [0xA0,0xA1] -> "A0A1") */
    public static String byteArrayToHexString(byte[] bytes) {
        if (bytes == null) return null;
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public static byte[] stringToByteArray(String text) {
        if (text == null) return null;
        return text.getBytes(Charset.forName("UTF-8"));
    }
}
