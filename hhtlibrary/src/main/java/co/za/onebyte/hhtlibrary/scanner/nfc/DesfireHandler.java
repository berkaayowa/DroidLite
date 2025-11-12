package co.za.onebyte.hhtlibrary.scanner.nfc;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import co.za.onebyte.hhtlibrary.utility.Common;

public class DesfireHandler {
    private final Tag tag;

    DesfireHandler(Tag tag) {
        this.tag = tag;
    }

    byte[] sendCommand(byte[] command) {
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

    boolean authenticate(byte[] key, byte keyNumber) {
        // DESFire authentication command structure
        byte[] authCommand = new byte[] {
                (byte) 0x90, // CLA
                (byte) 0x0A, // INS: Authenticate
                keyNumber,   // Key number (usually 0x00)
                (byte) 0x00, // P1
                (byte) 0x00, // P2
                (byte) 0x00  // Le
        };

        byte[] response = sendCommand(authCommand);
        if (response != null && response.length >= 2) {
            // Check if authentication was successful (SW1=0x91, SW2=0x00)
            return (response[response.length - 2] & 0xFF) == 0x91 &&
                    (response[response.length - 1] & 0xFF) == 0x00;
        }
        return false;
    }

    byte[] readData(int fileNumber, int offset, int length) {
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

        byte[] response = sendCommand(readCommand);
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

    boolean writeData(int fileNumber, int offset, byte[] data) {
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

        byte[] response = sendCommand(cmd.toByteArray());
        return response != null && response.length >= 2 &&
                (response[response.length - 2] & 0xFF) == 0x91 &&
                (response[response.length - 1] & 0xFF) == 0x00;
    }

    byte[] getVersion() {
        byte[] versionCommand = new byte[] {
                (byte) 0x90, // CLA
                (byte) 0x60, // INS: Get Version
                (byte) 0x00, // P1
                (byte) 0x00, // P2
                (byte) 0x00  // Le
        };

        return sendCommand(versionCommand);
    }

    boolean format() {
        byte[] formatCommand = new byte[] {
                (byte) 0x90, // CLA
                (byte) 0xFC, // INS: Format
                (byte) 0x00, // P1
                (byte) 0x00, // P2
                (byte) 0x00  // Le
        };

        byte[] response = sendCommand(formatCommand);
        return response != null && response.length >= 2 &&
                (response[response.length - 2] & 0xFF) == 0x91 &&
                (response[response.length - 1] & 0xFF) == 0x00;
    }

    boolean isDesfire() {
        return Arrays.asList(tag.getTechList()).contains(IsoDep.class.getName());
    }
}