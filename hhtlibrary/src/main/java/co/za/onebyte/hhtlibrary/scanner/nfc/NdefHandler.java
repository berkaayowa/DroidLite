package co.za.onebyte.hhtlibrary.scanner.nfc;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;

import co.za.onebyte.hhtlibrary.utility.Common;

public class NdefHandler {
    private final Tag tag;

    NdefHandler(Tag tag) {
        this.tag = tag;
    }

    String readNdef() {
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
            Common.log("NDEF read error: " + e.getMessage());
        } finally {
            try {
                if (ndef != null && ndef.isConnected()) {
                    ndef.close();
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    boolean writeText(String text) {
        NdefRecord textRecord = createTextRecord(text, Locale.ENGLISH, true);
        NdefMessage message = new NdefMessage(new NdefRecord[]{textRecord});
        return writeNdefMessage(message);
    }

    boolean writeUri(String uri) {
        NdefRecord uriRecord = NdefRecord.createUri(uri);
        NdefMessage message = new NdefMessage(new NdefRecord[]{uriRecord});
        return writeNdefMessage(message);
    }

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
            Common.log("NDEF write error: " + e.getMessage());
        }
        return false;
    }

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
}