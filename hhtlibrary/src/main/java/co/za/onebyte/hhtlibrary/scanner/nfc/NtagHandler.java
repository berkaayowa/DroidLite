package co.za.onebyte.hhtlibrary.scanner.nfc;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcA;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;

import co.za.onebyte.hhtlibrary.crypto.AesUtil;
import co.za.onebyte.hhtlibrary.utility.Common;
import co.za.onebyte.hhtlibrary.utility.Str;

/**
 * Handler for NTAG operations with NTAG424 DNA support
 */
/**
 * Handler for NTAG operations with NTAG424 DNA support
 */
/**
 * Handler for NTAG operations with complete read/write support
 */
/**
 * Handler for NTAG operations with proper connection management
 */
/**
 * Handler for NTAG operations with proper connection management
 */
public class NtagHandler {
    private final Tag tag;
    private final NfcA nfcA;
    private final IsoDep isoDep;
    private boolean isAuthenticated = false;
    private byte[] sessionKey = null;
    private String detectedType = null;
    public boolean isIsoMode = false;

    NtagHandler(Tag tag) {
        this.tag = tag;
        this.nfcA = NfcA.get(tag);
        this.isoDep = IsoDep.get(tag);
    }

    boolean isNtag() {
        detectNtagType();
        return detectedType.startsWith("NTAG");
    }

    String getNtagType() {
        detectNtagType();
        return detectedType;
    }

    /**
     * Debug method to verify mode detection
     */
    public void debugModeDetection() {
        Common.log("=== MODE DETECTION DEBUG ===");

        boolean hasIsoDep = false;
        boolean hasNfcA = false;
        boolean hasNdefFormatable = false;

        for (String tech : tag.getTechList()) {
            if (tech.contains("IsoDep")) hasIsoDep = true;
            if (tech.contains("NfcA")) hasNfcA = true;
            if (tech.contains("NdefFormatable")) hasNdefFormatable = true;
        }

        byte[] uid = tag.getId();

        Common.log("Has IsoDep: " + hasIsoDep);
        Common.log("Has NfcA: " + hasNfcA);
        Common.log("Has NdefFormatable: " + hasNdefFormatable);
        Common.log("UID Length: " + (uid != null ? uid.length : "null"));
        Common.log("UID Starts with 0x04: " + (uid != null && uid.length > 0 && uid[0] == 0x04));

        // Force re-detection
        detectedType = null;
        isIsoMode = false;
        detectNtagType();

        Common.log("Detected Type: " + detectedType);
        Common.log("ISO Mode Flag: " + isIsoMode);
    }

    private void detectNtagType() {

        debugModeDetection();

        if (detectedType != null) return;

        // Check characteristics first without connecting
        boolean hasIsoDep = false;
        boolean hasNfcA = false;
        boolean hasNdefFormatable = false;

        for (String tech : tag.getTechList()) {
            if (tech.contains("IsoDep")) hasIsoDep = true;
            if (tech.contains("NfcA")) hasNfcA = true;
            if (tech.contains("NdefFormatable")) hasNdefFormatable = true;
        }

        byte[] uid = tag.getId();

        // NTAG424 DNA characteristics based on your NFC Tool output
        if (hasIsoDep && hasNfcA && uid != null && uid.length == 7) {
            // Additional verification for NTAG424 DNA
            if (uid[0] == 0x04 && hasNdefFormatable) { // NXP manufacturer + NDEF formatable
                detectedType = "NTAG424 DNA (ISO Mode)";
                isIsoMode = true; // THIS WAS MISSING!
                return;
            }
        }

        // Check for standard NTAG tags
        if (hasNfcA && !hasIsoDep) {
            detectedType = detectStandardNtag();
            return;
        }

        detectedType = "Not NTAG";
    }

    /**
     * Detect NTAG type without causing connection conflicts
     */
//    private void detectNtagType() {
//        if (detectedType != null) return;
//
//        // Check characteristics first without connecting
//        boolean hasIsoDep = false;
//        boolean hasNfcA = false;
//
//        for (String tech : tag.getTechList()) {
//            if (tech.contains("IsoDep")) hasIsoDep = true;
//            if (tech.contains("NfcA")) hasNfcA = true;
//        }
//
//        byte[] uid = tag.getId();
//
//        // NTAG424 DNA characteristics based on your NFC Tool output
//        if (hasIsoDep && hasNfcA && uid != null && uid.length == 7) {
//            // This matches NTAG424 DNA characteristics
//            detectedType = "NTAG424 DNA (ISO Mode)";
//            isIsoMode = true;
//            return;
//        }
//
//        // If not NTAG424 DNA, check for other NTAG types
//        if (hasNfcA && !hasIsoDep) {
//            // Could be standard NTAG
//            detectedType = detectStandardNtag();
//            return;
//        }
//
//        detectedType = "Not NTAG";
//    }

    /**
     * Detect standard NTAG types (213, 215, 216)
     */
    private String detectStandardNtag() {
        if (nfcA == null) return "Not NTAG";

        try {
            nfcA.connect();
            nfcA.setTimeout(2000);

            // Try GET_VERSION command
            byte[] versionCmd = new byte[]{(byte) 0x60};
            byte[] response = nfcA.transceive(versionCmd);

            if (response != null && response.length >= 8) {
                if (response[0] == 0x00) {
                    switch (response[7] & 0xFF) {
                        case 0x03: return "NTAG 213";
                        case 0x04: return "NTAG 215";
                        case 0x05: return "NTAG 216";
                        case 0x06: return "NTAG424 DNA";
                        default: return "NTAG (Unknown)";
                    }
                }
            }

        } catch (IOException e) {
            // Version command failed
        } finally {
            safeClose(nfcA);
        }

        return "Not NTAG";
    }

    /**
     * Read a page from NTAG tag
     */
    public byte[] readPage(int pageIndex, byte[] password) {

        //detectNtagType();

        if (isIsoMode) {
            return readPageIsoMode(pageIndex, password);
        } else {
            return readPageNtagMode(pageIndex, password);
        }
    }

    /**
     * Write a page to NTAG tag
     */
    public boolean writePage(int pageIndex, byte[] data, byte[] password) {
        if (data == null || data.length != 4) {
            Common.log("Data must be exactly 4 bytes");
            return false;
        }

       // detectNtagType();

        if (isIsoMode) {
            return writePageIsoMode(pageIndex, data, password);
        } else {
            return writePageNtagMode(pageIndex, data, password);
        }
    }

    /**
     * Read page from NTAG424 DNA in ISO mode
     */
    private byte[] readPageIsoMode(int pageIndex, byte[] password) {
        if (isoDep == null) return null;

        try {
            // Close NfcA first if connected
            safeClose(nfcA);

            if (!isoDep.isConnected()) {
                isoDep.connect();
            }
            isoDep.setTimeout(5000);

            // Authenticate if password is provided
            if (password != null && !isAuthenticated) {
                if (!authenticateIsoMode(password)) {
                    Common.log("ISO mode authentication failed");
                    return null;
                }
            }

            // Read command for NTAG424 DNA in ISO mode
            byte[] readCmd = new byte[] {
                    (byte) 0x90, (byte) 0xAD, // READ command
                    0x00, 0x00, 0x00,         // File ID 0
                    (byte) pageIndex,         // Page number
                    0x00, 0x01,              // Read 1 page (4 bytes)
                    0x00                      // Le
            };

            byte[] response = isoDep.transceive(readCmd);

            if (response != null && response.length >= 6) {
                // Check for success status (91 00)
                if ((response[response.length - 2] & 0xFF) == 0x91 &&
                        (response[response.length - 1] & 0xFF) == 0x00) {

                    // Extract the page data (first 4 bytes)
                    byte[] data = new byte[4];
                    System.arraycopy(response, 0, data, 0, 4);
                    return data;
                }
            }

            return null;

        } catch (IOException e) {
            Common.log("ISO mode read error: " + e.getMessage());
            return null;
        } finally {
            safeClose(isoDep);
        }
    }

    /**
     * Write page to NTAG424 DNA in ISO mode
     */
    private boolean writePageIsoMode(int pageIndex, byte[] data, byte[] password) {
        if (isoDep == null) return false;

        try {
            // Close NfcA first if connected
            safeClose(nfcA);

            if (!isoDep.isConnected()) {
                isoDep.connect();
            }
            isoDep.setTimeout(5000);

            // Authenticate if password is provided
            if (password != null && !isAuthenticated) {
                if (!authenticateIsoMode(password)) {
                    Common.log("ISO mode authentication failed");
                    return false;
                }
            }

            // Write command for NTAG424 DNA in ISO mode
            byte[] writeCmd = new byte[] {
                    (byte) 0x90, (byte) 0x8D, // WRITE command
                    0x00, 0x00, 0x00,         // File ID 0
                    (byte) pageIndex,         // Page number
                    0x00, 0x01,              // Write 1 page (4 bytes)
                    0x04,                     // Data length
                    data[0], data[1], data[2], data[3], // Data
                    0x00                      // Le
            };

            byte[] response = isoDep.transceive(writeCmd);

            if (response != null && response.length >= 2) {
                // Check for success status (91 00)
                return (response[response.length - 2] & 0xFF) == 0x91 &&
                        (response[response.length - 1] & 0xFF) == 0x00;
            }

            return false;

        } catch (IOException e) {
            Common.log("ISO mode write error: " + e.getMessage());
            return false;
        } finally {
            safeClose(isoDep);
        }
    }

    /**
     * Read page from standard NTAG mode
     */
    private byte[] readPageNtagMode(int pageIndex, byte[] password) {
        if (nfcA == null) return null;

        try {
            // Close IsoDep first if connected
            safeClose(isoDep);

            if (!nfcA.isConnected()) {
                nfcA.connect();
            }
            nfcA.setTimeout(3000);

            // Authenticate if password is provided
            if (password != null) {
                if (!authenticateNtagMode(password)) {
                    Common.log("NTAG mode authentication failed");
                    return null;
                }
            }

            // Standard NTAG READ command
            byte[] readCmd = new byte[]{(byte) 0x30, (byte) pageIndex};
            return nfcA.transceive(readCmd);

        } catch (IOException e) {
            Common.log("NTAG mode read error: " + e.getMessage());
            return null;
        } finally {
            safeClose(nfcA);
        }
    }

    /**
     * Write page to standard NTAG mode
     */
    private boolean writePageNtagMode(int pageIndex, byte[] data, byte[] password) {
        if (nfcA == null) return false;

        try {
            // Close IsoDep first if connected
            safeClose(isoDep);

            if (!nfcA.isConnected()) {
                nfcA.connect();
            }
            nfcA.setTimeout(3000);

            // Authenticate if password is provided
            if (password != null) {
                if (!authenticateNtagMode(password)) {
                    Common.log("NTAG mode authentication failed");
                    return false;
                }
            }

            // Standard NTAG WRITE command
            byte[] writeCmd = new byte[6];
            writeCmd[0] = (byte) 0xA2; // WRITE command
            writeCmd[1] = (byte) pageIndex;
            System.arraycopy(data, 0, writeCmd, 2, 4);

            byte[] response = nfcA.transceive(writeCmd);

            // For NTAG, a successful write returns the written data
            return response != null && response.length >= 4;

        } catch (IOException e) {
            Common.log("NTAG mode write error: " + e.getMessage());
            return false;
        } finally {
            safeClose(nfcA);
        }
    }

    /**
     * Safely close a technology connection
     */
    private void safeClose(Object technology) {
        try {
            if (technology instanceof IsoDep) {
                IsoDep isoDep = (IsoDep) technology;
                if (isoDep.isConnected()) {
                    isoDep.close();
                }
            } else if (technology instanceof NfcA) {
                NfcA nfcA = (NfcA) technology;
                if (nfcA.isConnected()) {
                    nfcA.close();
                }
            }
        } catch (IOException e) {
            // Ignore close errors
        }
    }

    /**
     * Authenticate with NTAG424 DNA in ISO mode
     */
    private boolean authenticateIsoMode(byte[] password) {
        if (password == null || password.length != 16) {
            Common.log("NTAG424 DNA requires 16-byte AES key");
            return false;
        }

        try {
            // Close NfcA first if connected
            safeClose(nfcA);

            if (!isoDep.isConnected()) {
                isoDep.connect();
            }
            isoDep.setTimeout(5000);

            // Authentication command
            byte[] authCmd = new byte[] {
                    (byte) 0x90, (byte) 0xAA, // AUTHENTICATE command
                    0x00, 0x00, 0x00,         // File ID 0
                    0x00,                     // Key number (0 = default)
                    0x00                      // Le
            };

            byte[] response = isoDep.transceive(authCmd);

            if (response != null && response.length >= 16) {
                // Extract challenge (first 16 bytes)
                byte[] challenge = Arrays.copyOf(response, 16);

                // Encrypt challenge with AES key
                byte[] encryptedChallenge = AesUtil.encrypt(password, challenge);
                if (encryptedChallenge == null) {
                    Common.log("AES encryption failed");
                    return false;
                }

                // Send encrypted challenge
                byte[] responseCmd = new byte[5 + encryptedChallenge.length];
                responseCmd[0] = (byte) 0x90;
                responseCmd[1] = (byte) 0xAF; // AUTHENTICATE response
                responseCmd[2] = 0x00;
                responseCmd[3] = 0x00;
                responseCmd[4] = (byte) encryptedChallenge.length;
                System.arraycopy(encryptedChallenge, 0, responseCmd, 5, encryptedChallenge.length);

                byte[] authResponse = isoDep.transceive(responseCmd);

                if (authResponse != null && authResponse.length >= 2) {
                    // Check for success status (91 00)
                    if ((authResponse[authResponse.length - 2] & 0xFF) == 0x91 &&
                            (authResponse[authResponse.length - 1] & 0xFF) == 0x00) {
                        isAuthenticated = true;
                        return true;
                    }
                }
            }

            return false;

        } catch (IOException e) {
            Common.log("ISO mode auth error: " + e.getMessage());
            return false;
        } finally {
            safeClose(isoDep);
        }
    }

    /**
     * Authenticate with standard NTAG password
     */
    private boolean authenticateNtagMode(byte[] password) {
        if (password == null || password.length != 4) {
            Common.log("Standard NTAG requires 4-byte password");
            return false;
        }

        try {
            // Close IsoDep first if connected
            safeClose(isoDep);

            if (!nfcA.isConnected()) {
                nfcA.connect();
            }
            nfcA.setTimeout(3000);

            byte[] authCmd = new byte[6];
            authCmd[0] = (byte) 0x1B; // PWD_AUTH command
            System.arraycopy(password, 0, authCmd, 1, 4);
            authCmd[5] = (byte) 0x00; // CRC (simplified)

            byte[] response = nfcA.transceive(authCmd);

            // For NTAG, any response means authentication was attempted
            return response != null;

        } catch (IOException e) {
            Common.log("NTAG mode auth error: " + e.getMessage());
            return false;
        } finally {
            safeClose(nfcA);
        }
    }
}