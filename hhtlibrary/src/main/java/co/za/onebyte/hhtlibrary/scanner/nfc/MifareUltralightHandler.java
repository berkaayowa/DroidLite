package co.za.onebyte.hhtlibrary.scanner.nfc;

import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;

import java.io.IOException;
import java.util.Arrays;

import co.za.onebyte.hhtlibrary.utility.Common;

public class MifareUltralightHandler {
    private final Tag tag;

    MifareUltralightHandler(Tag tag) {
        this.tag = tag;
    }

    byte[] readPage(int pageIndex) {
        return readPage(pageIndex, null, false);
    }

    byte[] readPage(int pageIndex, byte[] key, boolean useKeyA) {
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

    boolean writePage(int pageIndex, byte[] data) {
        return writePage(pageIndex, data, null);
    }

    boolean writePage(int pageIndex, byte[] data, byte[] key) {
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

    String getType() {
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

    // Helper method for retry reading
    private byte[] readWithRetry(MifareUltralight mul, int pageIndex) {
        final int MAX_RETRIES = 3;
        final long RETRY_DELAY_MS = 100;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                if (!mul.isConnected()) {
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

    // Helper method to check if tag is still present using the existing connection
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
}