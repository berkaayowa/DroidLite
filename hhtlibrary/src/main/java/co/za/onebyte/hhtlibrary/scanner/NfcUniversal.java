package co.za.onebyte.hhtlibrary.scanner;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Parcelable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;

import co.za.onebyte.hhtlibrary.utility.Common;

public class NfcUniversal {

    private final Tag tag;

    public NfcUniversal(Tag tag) {
        this.tag = tag;
    }

    /** ------------------ GENERAL INFO ------------------ */

    /** Get UID of the tag in HEX format */
    public String getUid() {
        byte[] uid = tag.getId();
        StringBuilder sb = new StringBuilder();
        for (byte b : uid) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    /** Get supported techs */
    public String[] getSupportedTechs() {
        return tag.getTechList();
    }

    /** Detect and return type as human-readable string */
    public String getTagType() {
        for (String tech : tag.getTechList()) {

            Common.log("TagType: " + tech);

            if (tech.contains("MifareClassic")) return "MIFARE Classic";
            if (tech.contains("MifareUltralight")) return "MIFARE Ultralight";
            if (tech.contains("IsoDep")) return "DESFire / ISO-DEP";
            if (tech.contains("Ndef")) return "NDEF";
            if (tech.contains("NfcA")) return "NFC-A (ISO 14443-3A)";
            if (tech.contains("NfcB")) return "NFC-B (ISO 14443-3B)";
            if (tech.contains("NfcF")) return "NFC-F (Felica)";
            if (tech.contains("NfcV")) return "NFC-V (ISO 15693)";
        }
        return "Unknown / Unsupported";
    }

    /** ------------------ MIFARE CLASSIC ------------------ */

    public byte[] readMifareClassicBlock(int sectorIndex, int blockIndex, byte[] key, boolean useKeyA) {

        MifareClassic mfc = MifareClassic.get(tag);

        if (mfc == null) {

            Common.log("NFC Tag is null" );
            return null;
        }

        try {

            mfc.connect();
            if (key == null) {

                Common.log("Key is null" );

                key = MifareClassic.KEY_DEFAULT;
            }

            boolean auth = useKeyA
                    ? mfc.authenticateSectorWithKeyA(sectorIndex, key)
                    : mfc.authenticateSectorWithKeyB(sectorIndex, key);

            if (auth)
                return mfc.readBlock(blockIndex);
            else {
                Common.log("NFC Auth failed" );
            }

        } catch (IOException ex) {
            Common.log("NFC error|" + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try { mfc.close(); } catch (Exception ignored) {}
        }
        return null;
    }

    public boolean writeMifareClassicBlock(int sectorIndex, int blockIndex, byte[] data, byte[] key, boolean useKeyA) {
        MifareClassic mfc = MifareClassic.get(tag);
        if (mfc == null) return false;

        try {
            mfc.connect();
            if (key == null) key = MifareClassic.KEY_DEFAULT;

            boolean auth = useKeyA
                    ? mfc.authenticateSectorWithKeyA(sectorIndex, key)
                    : mfc.authenticateSectorWithKeyB(sectorIndex, key);

            if (auth) {
                mfc.writeBlock(blockIndex, data);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { mfc.close(); } catch (Exception ignored) {}
        }
        return false;
    }

    /** ------------------ MIFARE ULTRALIGHT ------------------ */

    public byte[] readMifareUltralightPage(int pageIndex) {
        MifareUltralight mul = MifareUltralight.get(tag);
        if (mul == null) return null;

        try {
            mul.connect();
            return mul.readPages(pageIndex);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { mul.close(); } catch (Exception ignored) {}
        }
        return null;
    }

    public boolean writeMifareUltralightPage(int pageIndex, byte[] data) {
        MifareUltralight mul = MifareUltralight.get(tag);
        if (mul == null) return false;

        try {
            mul.connect();
            mul.writePage(pageIndex, data);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { mul.close(); } catch (Exception ignored) {}
        }
        return false;
    }

    /** ------------------ NDEF SUPPORT ------------------ */

    /** Read NDEF Text or URL */
    public String readNdef() {
        Ndef ndef = Ndef.get(tag);
        if (ndef == null) return null;

        try {
            ndef.connect();
            NdefMessage ndefMessage = ndef.getNdefMessage();
            if (ndefMessage != null) {
                for (NdefRecord record : ndefMessage.getRecords()) {
                    if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN) {
                        if (Arrays.equals(record.getType(), NdefRecord.RTD_TEXT)) {
                            return parseTextRecord(record);
                        } else if (Arrays.equals(record.getType(), NdefRecord.RTD_URI)) {
                            return parseUriRecord(record);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { ndef.close(); } catch (Exception ignored) {}
        }
        return null;
    }

    /** Write NDEF Text */
    public boolean writeNdefText(String text) {
        NdefRecord textRecord = createTextRecord(text, Locale.ENGLISH, true);
        NdefMessage message = new NdefMessage(new NdefRecord[]{textRecord});
        return writeNdefMessage(message);
    }

    /** Write NDEF URI */
    public boolean writeNdefUri(String uri) {
        NdefRecord uriRecord = NdefRecord.createUri(uri);
        NdefMessage message = new NdefMessage(new NdefRecord[]{uriRecord});
        return writeNdefMessage(message);
    }

    /** Internal: write NDEF message */
    private boolean writeNdefMessage(NdefMessage message) {
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) return false;
                ndef.writeNdefMessage(message);
                return true;
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    format.connect();
                    format.format(message);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /** ------------------ HELPER METHODS ------------------ */

    private String parseTextRecord(NdefRecord record) {
        try {
            byte[] payload = record.getPayload();
            int langCodeLen = payload[0] & 0x3F;
            return new String(payload, 1 + langCodeLen, payload.length - 1 - langCodeLen, Charset.forName("UTF-8"));
        } catch (Exception e) {
            return null;
        }
    }

    private String parseUriRecord(NdefRecord record) {
        try {
            byte[] payload = record.getPayload();
            return new String(payload, 1, payload.length - 1, Charset.forName("UTF-8"));
        } catch (Exception e) {
            return null;
        }
    }

    private NdefRecord createTextRecord(String text, Locale locale, boolean encodeInUtf8) {
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));
        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
        byte[] textBytes = text.getBytes(utfEncoding);
        int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
    }

//    public byte[] readMifareUltralightPage(int pageIndex, byte[] key, boolean useKeyA) {
//        MifareUltralight mul = MifareUltralight.get(tag);
//        if (mul == null) {
//            Common.log("MIFARE Ultralight tag is null");
//            return null;
//        }
//
//        try {
//            mul.connect();
//
//            // Check if this is Ultralight C (supports authentication)
//            if (mul.getType() == MifareUltralight.TYPE_ULTRALIGHT_C) {
//                // Ultralight C authentication process
//                if (key != null && key.length >= 16) {
//                    try {
//                        // Ultralight C uses a 16-byte key for authentication
//                        byte[] authCmd = new byte[2 + 16]; // Fixed 16-byte key length
//                        authCmd[0] = (byte) 0x1A; // AUTH command
//                        authCmd[1] = (byte) 0x00; // Auth mode (0x00 for Auth)
//
//                        // Copy key (ensure it's exactly 16 bytes)
//                        byte[] authKey = new byte[16];
//                        System.arraycopy(key, 0, authKey, 0, Math.min(key.length, 16));
//                        if (key.length < 16) {
//                            // Pad with zeros if key is shorter than 16 bytes
//                            Arrays.fill(authKey, key.length, 16, (byte) 0x00);
//                        }
//                        System.arraycopy(authKey, 0, authCmd, 2, 16);
//
//                        byte[] response = mul.transceive(authCmd);
//                        if (response != null && response.length > 0 && response[0] == (byte) 0x00) {
//                            Common.log("Ultralight C authentication successful");
//                        } else {
//                            Common.log("Ultralight C authentication failed");
//                            return null;
//                        }
//                    } catch (IOException e) {
//                        Common.log("Ultralight C auth error: " + e.getMessage());
//                        return null;
//                    }
//                } else if (key != null) {
//                    Common.log("Ultralight C requires 16-byte key");
//                    return null;
//                }
//            } else if (key != null) {
//                Common.log("This Ultralight variant does not support authentication|" + mul.getType());
//                // Continue reading without authentication for non-C variants
//            }
//
//            // Read the page
//            return mul.readPages(pageIndex);
//
//        } catch (IOException e) {
//            Common.log("NFC Ultralight error: " + e.getMessage());
//            e.printStackTrace();
//        } finally {
//            try { mul.close(); } catch (Exception ignored) {}
//        }
//        return null;
//    }

    /** ------------------ MIFARE ULTRALIGHT ------------------ */

//    public byte[] readMifareUltralightPage(int pageIndex) {
//        return readMifareUltralightPage(pageIndex, null, false);
//    }

    public byte[] readMifareUltralightPage(int pageIndex, byte[] key, boolean useKeyA) {
        MifareUltralight mul = MifareUltralight.get(tag);
        if (mul == null) {
            Common.log("MIFARE Ultralight tag is null");
            return null;
        }

        try {
            if (!mul.isConnected()) {
                mul.connect();
            }

            // Check if this is Ultralight C (supports authentication)
            if (mul.getType() == MifareUltralight.TYPE_ULTRALIGHT_C) {
                // Ultralight C authentication process
                if (key != null && key.length >= 16) {
                    try {
                        // Ultralight C uses a 16-byte key for authentication
                        byte[] authCmd = new byte[2 + 16];
                        authCmd[0] = (byte) 0x1A; // AUTH command
                        authCmd[1] = (byte) 0x00; // Auth mode (0x00 for Auth)

                        // Copy key (ensure it's exactly 16 bytes)
                        byte[] authKey = new byte[16];
                        System.arraycopy(key, 0, authKey, 0, Math.min(key.length, 16));
                        if (key.length < 16) {
                            Arrays.fill(authKey, key.length, 16, (byte) 0x00);
                        }
                        System.arraycopy(authKey, 0, authCmd, 2, 16);

                        byte[] response = mul.transceive(authCmd);
                        if (response != null && response.length > 0 && response[0] == (byte) 0x00) {
                            Common.log("Ultralight C authentication successful");
                        } else {
                            Common.log("Ultralight C authentication failed");
                            return null;
                        }
                    } catch (IOException e) {
                        Common.log("Ultralight C auth error: " + e.getMessage());
                        return null;
                    }
                } else if (key != null) {
                    Common.log("Ultralight C requires 16-byte key");
                    return null;
                }
            } else if (key != null) {
                Common.log("This Ultralight variant does not support authentication");
                // Continue reading without authentication for non-C variants
            }

            // Read the page with retry logic
            return readWithRetry(mul, pageIndex);

        } catch (IOException e) {
            Common.log("NFC Ultralight connection error: " + e.getMessage());
            return null;
        } finally {
            try {
                if (mul != null && mul.isConnected()) {
                    mul.close();
                }
            } catch (Exception ignored) {}
        }
    }

    public boolean writeMifareUltralightPage(int pageIndex, byte[] data, byte[] key) {
        MifareUltralight mul = MifareUltralight.get(tag);
        if (mul == null) {
            Common.log("MIFARE Ultralight tag is null");
            return false;
        }

        try {
            if (!mul.isConnected()) {
                mul.connect();
            }

            // Authentication for Ultralight C
            if (mul.getType() == MifareUltralight.TYPE_ULTRALIGHT_C && key != null) {
                if (key.length >= 16) {
                    try {
                        byte[] authCmd = new byte[2 + 16];
                        authCmd[0] = (byte) 0x1A;
                        authCmd[1] = (byte) 0x00;

                        byte[] authKey = new byte[16];
                        System.arraycopy(key, 0, authKey, 0, Math.min(key.length, 16));
                        if (key.length < 16) {
                            Arrays.fill(authKey, key.length, 16, (byte) 0x00);
                        }
                        System.arraycopy(authKey, 0, authCmd, 2, 16);

                        byte[] response = mul.transceive(authCmd);
                        if (response == null || response.length == 0 || response[0] != (byte) 0x00) {
                            Common.log("Ultralight C write authentication failed");
                            return false;
                        }
                    } catch (IOException e) {
                        Common.log("Ultralight C auth error: " + e.getMessage());
                        return false;
                    }
                } else {
                    Common.log("Ultralight C requires 16-byte key for write");
                    return false;
                }
            }

            // Write with retry logic and tag presence check
            return writeWithRetry(mul, pageIndex, data);

        } catch (IOException e) {
            Common.log("NFC Ultralight connection error: " + e.getMessage());
            return false;
        } finally {
            try {
                if (mul != null && mul.isConnected()) {
                    mul.close();
                }
            } catch (Exception ignored) {}
        }
    }

    // Helper method for retry reading
    private byte[] readWithRetry(MifareUltralight mul, int pageIndex) {
        final int MAX_RETRIES = 3;
        final long RETRY_DELAY_MS = 100;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                if (!mul.isConnected()) {
                    // Don't reconnect here - use the existing connection
                    Common.log("Connection lost during read, attempt " + attempt);
                    return null;
                }
                return mul.readPages(pageIndex);
            } catch (IOException e) {
                if (e instanceof android.nfc.TagLostException) {
                    Common.log("Tag lost during read, attempt " + attempt + " of " + MAX_RETRIES);
                    if (attempt < MAX_RETRIES) {
                        try {
                            Thread.sleep(RETRY_DELAY_MS);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            return null;
                        }
                        continue;
                    }
                }
                Common.log("Read error: " + e.getMessage());
                return null;
            }
        }
        return null;
    }

    // Helper method for retry writing
    private boolean writeWithRetry(MifareUltralight mul, int pageIndex, byte[] data) {
        final int MAX_RETRIES = 3;
        final long RETRY_DELAY_MS = 100;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                if (!mul.isConnected()) {
                    // Don't reconnect here - use the existing connection
                    Common.log("Connection lost during write, attempt " + attempt);
                    return false;
                }

                // Check if tag is still present before writing using a simple test
                if (!isTagPresent(mul)) {
                    Common.log("Tag not present before write, attempt " + attempt);
                    if (attempt < MAX_RETRIES) {
                        try {
                            Thread.sleep(RETRY_DELAY_MS);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            return false;
                        }
                        continue;
                    }
                    return false;
                }

                mul.writePage(pageIndex, data);
                return true;

            } catch (IOException e) {
                if (e instanceof android.nfc.TagLostException) {
                    Common.log("Tag lost during write, attempt " + attempt + " of " + MAX_RETRIES);
                    if (attempt < MAX_RETRIES) {
                        try {
                            Thread.sleep(RETRY_DELAY_MS);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            return false;
                        }
                        continue;
                    }
                }
                Common.log("Write error: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    // Modified helper method to check if tag is still present using the existing connection
    private boolean isTagPresent(MifareUltralight mul) {
        try {
            if (!mul.isConnected()) {
                return false;
            }
            // Try to read page 0 (usually contains manufacturer data)
            // This is a lightweight check that uses the existing connection
            byte[] testRead = mul.readPages(0);
            return testRead != null && testRead.length > 0;
        } catch (IOException e) {
            // Tag is not present or communication failed
            Common.log("Tag presence check failed: " + e.getMessage());
            return false;
        }
    }

    // New method to check tag presence without creating new connections
    public boolean isTagStillPresent() {
        try {
            // Use the existing tag instance to check presence
            // This is a safer approach that doesn't create new technology instances
            byte[] id = tag.getId();
            return id != null && id.length > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // Helper method to get Ultralight type
    public String getUltralightType() {
        MifareUltralight mul = MifareUltralight.get(tag);
        if (mul == null) return "Not Ultralight";

        switch (mul.getType()) {
            case MifareUltralight.TYPE_ULTRALIGHT:
                return "MIFARE Ultralight";
            case MifareUltralight.TYPE_ULTRALIGHT_C:
                return "MIFARE Ultralight C";
            case MifareUltralight.TYPE_UNKNOWN:
            default:
                return "Unknown Ultralight";
        }
    }

    public byte[] sendDesfireCommand(byte[] command) {
        IsoDep isoDep = IsoDep.get(tag);
        if (isoDep == null) {
            Common.log("ISO-DEP tag is null");
            return null;
        }

        try {
            if (!isoDep.isConnected()) {
                isoDep.connect();
                isoDep.setTimeout(5000); // 5 second timeout
            }

            return isoDep.transceive(command);
        } catch (IOException e) {
            Common.log("ISO-DEP command error: " + e.getMessage());
            return null;
        } finally {
            try {
                if (isoDep != null && isoDep.isConnected()) {
                    isoDep.close();
                }
            } catch (Exception ignored) {}
        }
    }

    public boolean desfireAuthenticate(byte[] key, byte keyNumber) {
        // DESFire authentication command structure
        byte[] authCommand = new byte[] {
                (byte) 0x90, // CLA
                (byte) 0x0A, // INS: Authenticate
                keyNumber,   // Key number (usually 0x00)
                (byte) 0x00, // P1
                (byte) 0x00, // P2
                (byte) 0x00  // Le
        };

        byte[] response = sendDesfireCommand(authCommand);
        if (response != null && response.length >= 2) {
            // Check if authentication was successful (SW1=0x91, SW2=0x00)
            return (response[response.length - 2] & 0xFF) == 0x91 &&
                    (response[response.length - 1] & 0xFF) == 0x00;
        }
        return false;
    }

    public byte[] desfireReadData(int fileNumber, int offset, int length) {
        // Read data command
        byte[] readCommand = new byte[] {
                (byte) 0x90, // CLA
                (byte) 0xBD, // INS: Read Data
                (byte) fileNumber, // File number
                (byte) (offset >> 16), // Offset byte 2
                (byte) (offset >> 8),  // Offset byte 1
                (byte) offset,         // Offset byte 0
                (byte) 0x00, // P2
                (byte) length // Length
        };

        byte[] response = sendDesfireCommand(readCommand);
        if (response != null && response.length >= 2) {
            // Check if read was successful
            if ((response[response.length - 2] & 0xFF) == 0x91 &&
                    (response[response.length - 1] & 0xFF) == 0x00) {
                // Return data without status bytes
                byte[] data = new byte[response.length - 2];
                System.arraycopy(response, 0, data, 0, data.length);
                return data;
            }
        }
        return null;
    }

    public boolean desfireWriteData(int fileNumber, int offset, byte[] data) {
        // Write data command
        ByteArrayOutputStream cmd = new ByteArrayOutputStream();
        cmd.write(0x90); // CLA
        cmd.write(0x3D); // INS: Write Data
        cmd.write(fileNumber); // File number
        cmd.write(offset >> 16); // Offset byte 2
        cmd.write(offset >> 8);  // Offset byte 1
        cmd.write(offset);       // Offset byte 0
        cmd.write(0x00); // P2
        cmd.write(data.length); // Length
        cmd.write(data, 0, data.length); // Data

        byte[] response = sendDesfireCommand(cmd.toByteArray());
        return response != null && response.length >= 2 &&
                (response[response.length - 2] & 0xFF) == 0x91 &&
                (response[response.length - 1] & 0xFF) == 0x00;
    }

    public byte[] desfireGetVersion() {
        byte[] versionCommand = new byte[] {
                (byte) 0x90, // CLA
                (byte) 0x60, // INS: Get Version
                (byte) 0x00, // P1
                (byte) 0x00, // P2
                (byte) 0x00  // Le
        };

        return sendDesfireCommand(versionCommand);
    }

    public boolean desfireFormat() {
        byte[] formatCommand = new byte[] {
                (byte) 0x90, // CLA
                (byte) 0xFC, // INS: Format
                (byte) 0x00, // P1
                (byte) 0x00, // P2
                (byte) 0x00  // Le
        };

        byte[] response = sendDesfireCommand(formatCommand);
        return response != null && response.length >= 2 &&
                (response[response.length - 2] & 0xFF) == 0x91 &&
                (response[response.length - 1] & 0xFF) == 0x00;
    }

    // Helper method to check if tag is DESFire
    public boolean isDesfire() {
        return Arrays.asList(tag.getTechList()).contains(IsoDep.class.getName());
    }
}
