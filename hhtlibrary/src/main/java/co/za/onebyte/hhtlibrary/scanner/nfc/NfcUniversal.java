package co.za.onebyte.hhtlibrary.scanner.nfc;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import co.za.onebyte.hhtlibrary.utility.Common;
import co.za.onebyte.hhtlibrary.utility.Str;

/**
 * Universal NFC tag reader/writer supporting multiple tag technologies
 * Provides a unified interface for MIFARE Classic, MIFARE Ultralight, NDEF, and DESFire operations
 */
public class NfcUniversal {
    private final Tag tag;
    private final MifareClassicHandler mifareClassicHandler;
    private final MifareUltralightHandler mifareUltralightHandler;
    private final NdefHandler ndefHandler;
    private final DesfireHandler desfireHandler;
    private final NtagHandler ntagHandler;

    /**
     * Constructor for NfcUniversal
     * @param tag The NFC tag to operate on
     */
    public NfcUniversal(Tag tag) {
        this.tag = tag;
        this.mifareClassicHandler = new MifareClassicHandler(tag);
        this.mifareUltralightHandler = new MifareUltralightHandler(tag);
        this.ndefHandler = new NdefHandler(tag);
        this.desfireHandler = new DesfireHandler(tag);
        this.ntagHandler = new NtagHandler(tag);
    }

    /** ------------------ GENERAL INFO ------------------ */

    /**
     * Get UID of the tag in HEX format
     * @return HEX string representation of the tag UID
     */
    public String getUid() {
        byte[] uid = tag.getId();
        StringBuilder sb = new StringBuilder();
        for (byte b : uid) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    /**
     * Get supported technologies for this tag
     * @return Array of supported technology strings
     */
    public String[] getSupportedTechs() {
        return tag.getTechList();
    }

    /**
     * Detect and return tag type as human-readable string
     * @return Human-readable tag type description
     */
//    public String getTagType() {
//        for (String tech : tag.getTechList()) {
//            if (tech.contains("MifareClassic")) return "MIFARE Classic";
//            if (tech.contains("MifareUltralight")) return "MIFARE Ultralight";
//            if (tech.contains("NdefFormatable")) return "Formatable NDEF";
//            if (tech.contains("IsoDep")) return "DESFire / ISO-DEP";
//            if (tech.contains("Ndef")) return "NDEF";
//            if (tech.contains("NfcA")) return "NFC-A (ISO 14443-3A)";
//            if (tech.contains("NfcB")) return "NFC-B (ISO 14443-3B)";
//            if (tech.contains("NfcF")) return "NFC-F (Felica)";
//            if (tech.contains("NfcV")) return "NFC-V (ISO 15693)";
//        }
//        return "Unknown / Unsupported";
//    }

    /**
     * Get tag type without causing connection conflicts
     */
    public String getTagType() {
        // Analyze technologies without connecting
        boolean hasIsoDep = false;
        boolean hasNfcA = false;
        boolean hasNdef = false;
        boolean hasNdefFormatable = false;
        boolean hasMifareClassic = false;
        boolean hasMifareUltralight = false;
        boolean hasNfcB = false;
        boolean hasNfcF = false;
        boolean hasNfcV = false;

        for (String tech : tag.getTechList()) {
            if (tech.contains("IsoDep")) hasIsoDep = true;
            if (tech.contains("NfcA")) hasNfcA = true;
            if (tech.contains("Ndef")) hasNdef = true;
            if (tech.contains("NdefFormatable")) hasNdefFormatable = true;
            if (tech.contains("MifareClassic")) hasMifareClassic = true;
            if (tech.contains("MifareUltralight")) hasMifareUltralight = true;
            if (tech.contains("NfcB")) hasNfcB = true;
            if (tech.contains("NfcF")) hasNfcF = true;
            if (tech.contains("NfcV")) hasNfcV = true;
        }

        byte[] uid = tag.getId();

        // NTAG424 DNA characteristics
        if (hasIsoDep && hasNfcA && uid != null && uid.length == 7) {
            return "NTAG424 DNA (ISO Mode)";
        }

        // DESFire / ISO-DEP
        if (hasIsoDep) {
            return "DESFire / ISO-DEP";
        }

        // MIFARE Classic
        if (hasMifareClassic) {
            return "MIFARE Classic";
        }

        // MIFARE Ultralight
        if (hasMifareUltralight) {
            return "MIFARE Ultralight";
        }

        // Standard NTAG tags (try to detect specific type)
        if (hasNfcA && !hasIsoDep && !hasMifareClassic && !hasMifareUltralight) {
            String ntagType = ntagHandler.getNtagType();
            if (!ntagType.equals("Not NTAG")) {
                return ntagType;
            }
        }

        // NDEF tag
        if (hasNdef) {
            return "NDEF";
        }

        // NDEF Formatable tag
        if (hasNdefFormatable) {
            return "NDEF Formatable";
        }

        // NFC-B
        if (hasNfcB) {
            return "NFC-B (ISO 14443-3B)";
        }

        // NFC-F (Felica)
        if (hasNfcF) {
            return "NFC-F (Felica)";
        }

        // NFC-V (ISO 15693)
        if (hasNfcV) {
            return "NFC-V (ISO 15693)";
        }

        // Generic types based on technology
        if (hasNfcA) return "NFC-A (ISO 14443-3A)";
        if (hasNfcB) return "NFC-B (ISO 14443-3B)";
        if (hasNfcF) return "NFC-F (Felica)";
        if (hasNfcV) return "NFC-V (ISO 15693)";

        return "Unknown / Unsupported";
    }

    /**
     * Check if tag is NTAG compatible
     * @return True if tag is an NTAG tag, false otherwise
     */
    public boolean isNtag() {
        return ntagHandler.isNtag();
    }

    /**
     * Get NTAG type
     * @return Human-readable NTAG type description
     */
    public String getNtagType() {
        return ntagHandler.getNtagType();
    }

    public boolean isNtag424Dna() {
        boolean hasIsoDep = false;
        boolean hasNfcA = false;

        for (String tech : tag.getTechList()) {
            if (tech.contains("IsoDep")) hasIsoDep = true;
            if (tech.contains("NfcA")) hasNfcA = true;
        }

        byte[] uid = tag.getId();
        return hasIsoDep && hasNfcA && uid != null && uid.length == 7;
    }

    /**
     * Force detect as NTAG424 DNA based on characteristics
     */
//    public boolean forceDetectAsNtag424Dna() {
//        return ntagHandler.forceDetectAsNtag424Dna();
//    }

    /**
     * Get detailed tag information
     */
    public Map<String, String> getTagDetails() {
        Map<String, String> details = new HashMap<>();

        details.put("UID", getUid());
        details.put("Type", getTagType());
        details.put("NTAG Type", getNtagType());
        details.put("ISO Mode", String.valueOf(isIsoMode()));

        // Add technologies
        details.put("Technologies", Arrays.toString(getSupportedTechs()));

        // Add UID bytes for analysis
        byte[] uid = tag.getId();

        if (uid != null) {
            details.put("UID Length", String.valueOf(uid.length));
            details.put("UID Bytes", Str.byteArrayToHexString(uid));
        }

        return details;
    }

    /** ------------------ NTAG OPERATIONS ------------------ */

    /**
     * Read a page from NTAG tag (with optional authentication)
     * @param pageIndex Page index to read from
     * @param password Password for authentication (null if no authentication needed)
     * @return Page data or null if operation failed
     */
    public byte[] readNtagPage(int pageIndex, byte[] password) {
        return ntagHandler.readPage(pageIndex, password);
    }

    /**
     * Write a page to NTAG tag (with optional authentication)
     * @param pageIndex Page index to write to
     * @param data Data to write (4 bytes)
     * @param password Password for authentication (null if no authentication needed)
     * @return True if operation succeeded, false otherwise
     */
    public boolean writeNtagPage(int pageIndex, byte[] data, byte[] password) {
        return ntagHandler.writePage(pageIndex, data, password);
    }

    public boolean isIsoMode() {
        return ntagHandler.isIsoMode;
    }

    /**
     * Read NDEF message from NTAG tag (with optional authentication)
     * @param password Password for authentication (null if no authentication needed)
     * @return NDEF message as string or null if operation failed
     */
//    public String readNtagNdef(byte[] password) {
//        return ntagHandler.readNdef(password);
//    }

    /**
     * Write NDEF message to NTAG tag (with optional authentication)
     * @param text Text to write as NDEF
     * @param password Password for authentication (null if no authentication needed)
     * @return True if operation succeeded, false otherwise
     */
//    public boolean writeNtagNdefText(String text, byte[] password) {
//        return ntagHandler.writeNdefText(text, password);
//    }

    /**
     * Write NDEF URI to NTAG tag (with optional authentication)
     * @param uri URI to write as NDEF
     * @param password Password for authentication (null if no authentication needed)
     * @return True if operation succeeded, false otherwise
     */
//    public boolean writeNtagNdefUri(String uri, byte[] password) {
//        return ntagHandler.writeNdefUri(uri, password);
//    }

    /**
     * Set password protection for NTAG tag
     * @param password 4-byte password to set
     * @param pack 2-byte PACK value (password acknowledge)
     * @return True if operation succeeded, false otherwise
     */
//    public boolean setNtagPassword(byte[] password, byte[] pack) {
//        return ntagHandler.setPassword(password, pack);
//    }

    /**
     * Authenticate with NTAG tag using password
     * @param password 4-byte password for authentication
     * @return True if authentication succeeded, false otherwise
     */
//    public boolean authenticateNtag(byte[] password) {
//        return ntagHandler.authenticate(password);
//    }

    /** ------------------ DELEGATE METHODS ------------------ */

    /**
     * Read a block from MIFARE Classic tag
     * @param sectorIndex Sector index to read from
     * @param blockIndex Block index to read from
     * @param key Authentication key (null for default key)
     * @param useKeyA Whether to use Key A (true) or Key B (false) for authentication
     * @return Block data or null if operation failed
     */
    public byte[] readMifareClassicBlock(int sectorIndex, int blockIndex, byte[] key, boolean useKeyA) {
        return mifareClassicHandler.readBlock(sectorIndex, blockIndex, key, useKeyA);
    }

    /**
     * Write a block to MIFARE Classic tag
     * @param sectorIndex Sector index to write to
     * @param blockIndex Block index to write to
     * @param data Data to write
     * @param key Authentication key (null for default key)
     * @param useKeyA Whether to use Key A (true) or Key B (false) for authentication
     * @return True if operation succeeded, false otherwise
     */
    public boolean writeMifareClassicBlock(int sectorIndex, int blockIndex, byte[] data, byte[] key, boolean useKeyA) {
        return mifareClassicHandler.writeBlock(sectorIndex, blockIndex, data, key, useKeyA);
    }

    /**
     * Read a page from MIFARE Ultralight tag (without authentication)
     * @param pageIndex Page index to read from
     * @return Page data or null if operation failed
     */
    public byte[] readMifareUltralightPage(int pageIndex) {
        return mifareUltralightHandler.readPage(pageIndex);
    }

    /**
     * Read a page from MIFARE Ultralight tag (with authentication for Ultralight C)
     * @param pageIndex Page index to read from
     * @param key Authentication key (16 bytes for Ultralight C)
     * @param useKeyA Whether to use Key A authentication (reserved for future use)
     * @return Page data or null if operation failed
     */
    public byte[] readMifareUltralightPage(int pageIndex, byte[] key, boolean useKeyA) {
        return mifareUltralightHandler.readPage(pageIndex, key, useKeyA);
    }

    /**
     * Write a page to MIFARE Ultralight tag (without authentication)
     * @param pageIndex Page index to write to
     * @param data Data to write
     * @return True if operation succeeded, false otherwise
     */
    public boolean writeMifareUltralightPage(int pageIndex, byte[] data) {
        return mifareUltralightHandler.writePage(pageIndex, data);
    }

    /**
     * Write a page to MIFARE Ultralight tag (with authentication for Ultralight C)
     * @param pageIndex Page index to write to
     * @param data Data to write
     * @param key Authentication key (16 bytes for Ultralight C)
     * @return True if operation succeeded, false otherwise
     */
    public boolean writeMifareUltralightPage(int pageIndex, byte[] data, byte[] key) {
        return mifareUltralightHandler.writePage(pageIndex, data, key);
    }

    /**
     * Read NDEF content from tag
     * @return NDEF content as string or null if no NDEF data found
     */
    public String readNdef() {
        return ndefHandler.readNdef();
    }

    /**
     * Write text as NDEF record to tag
     * @param text Text to write
     * @return True if operation succeeded, false otherwise
     */
    public boolean writeNdefText(String text) {
        return ndefHandler.writeText(text);
    }

    /**
     * Write URI as NDEF record to tag
     * @param uri URI to write
     * @return True if operation succeeded, false otherwise
     */
    public boolean writeNdefUri(String uri) {
        return ndefHandler.writeUri(uri);
    }

    /**
     * Send a raw command to DESFire tag
     * @param command Command bytes to send
     * @return Response from tag or null if operation failed
     */
    public byte[] sendDesfireCommand(byte[] command) {
        return desfireHandler.sendCommand(command);
    }

    /**
     * Authenticate with DESFire tag
     * @param key Authentication key
     * @param keyNumber Key number to use for authentication
     * @return True if authentication succeeded, false otherwise
     */
    public boolean desfireAuthenticate(byte[] key, byte keyNumber) {
        return desfireHandler.authenticate(key, keyNumber);
    }

    /**
     * Read data from DESFire tag
     * @param fileNumber File number to read from
     * @param offset Offset within file to read from
     * @param length Number of bytes to read
     * @return Data read from tag or null if operation failed
     */
    public byte[] desfireReadData(int fileNumber, int offset, int length) {
        return desfireHandler.readData(fileNumber, offset, length);
    }

    /**
     * Write data to DESFire tag
     * @param fileNumber File number to write to
     * @param offset Offset within file to write to
     * @param data Data to write
     * @return True if operation succeeded, false otherwise
     */
    public boolean desfireWriteData(int fileNumber, int offset, byte[] data) {
        return desfireHandler.writeData(fileNumber, offset, data);
    }

    /**
     * Get version information from DESFire tag
     * @return Version data or null if operation failed
     */
    public byte[] desfireGetVersion() {
        return desfireHandler.getVersion();
    }

    /**
     * Format DESFire tag
     * @return True if operation succeeded, false otherwise
     */
    public boolean desfireFormat() {
        return desfireHandler.format();
    }

    /**
     * Check if tag is DESFire compatible
     * @return True if tag supports DESFire, false otherwise
     */
    public boolean isDesfire() {
        return desfireHandler.isDesfire();
    }

    /**
     * Get MIFARE Ultralight type
     * @return Human-readable Ultralight type description
     */
    public String getUltralightType() {
        return mifareUltralightHandler.getType();
    }

    /**
     * Check if tag is still present
     * @return True if tag is still present, false otherwise
     */
    public boolean isTagStillPresent() {
        try {
            byte[] id = tag.getId();
            return id != null && id.length > 0;
        } catch (Exception e) {
            return false;
        }
    }

}