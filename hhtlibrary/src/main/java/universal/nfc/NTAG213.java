package universal.nfc;


import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;

import co.za.onebyte.hhtlibrary.utility.Str;

public class NTAG213 {

    private static final String TAG = "Ntag213Helper";

    private byte[] password;

    public NTAG213(String hexPassword) {
        this.password = hexStringToByteArray(hexPassword);
    }

    /**
     * Dumps the writable pages array for debugging
     * @param writablePages boolean[40] array from getWritablePages()
     */
    public void dumpWritablePages(boolean[] writablePages) {
        if (writablePages == null || writablePages.length != 40) {
            Log.e(TAG, "Invalid writable pages array");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Writable pages: ");
        for (int i = 0; i < writablePages.length; i++) {
            if (writablePages[i]) {
                sb.append(i).append(" "); // writable page
            }
        }
        Log.d(TAG, sb.toString());

        // Optional: also show protected pages
        sb.setLength(0);
        sb.append("Protected pages: ");
        for (int i = 0; i < writablePages.length; i++) {
            if (!writablePages[i]) {
                sb.append(i).append(" "); // protected page
            }
        }
        Log.d(TAG, sb.toString());
    }

    /**
     * Check which NTAG213 pages are writable
     * Returns an array of booleans: true = writable, false = protected
     */
    public boolean[] getWritablePages(Tag tag) {
        MifareUltralight ultralight = MifareUltralight.get(tag);
        boolean[] writablePages = new boolean[40]; // NTAG213 has pages 0-39

        // Default: pages 4-39 are potentially writable
        for (int i = 0; i < 4; i++) writablePages[i] = false; // system pages 0-3
        for (int i = 4; i < 40; i++) writablePages[i] = true;

        if (ultralight == null) {
            Log.e(TAG, "MifareUltralight not supported.");
            return writablePages;
        }

        try {
            ultralight.connect();
            Thread.sleep(10);
            // Read AUTH0 and ACCESS (page 42)
            byte[] cfg42 = ultralight.transceive(new byte[]{0x30, 42});
            if (cfg42 != null && cfg42.length >= 2) {
                int auth0 = cfg42[0] & 0xFF; // first protected page
                int access = cfg42[1] & 0xFF; // protection bits

                Log.d(TAG, "AUTH0 = " + auth0 + ", ACCESS = " + access);

                // Pages >= AUTH0 require authentication
                for (int i = auth0; i < 40; i++) {
                    writablePages[i] = false; // mark protected pages as non-writable
                }

                // Access register bits: bit 6 = permanent lock for some pages (0-31)
                if ((access & 0x40) != 0) {
                    for (int i = 4; i <= 31; i++) writablePages[i] = false;
                }

                // Access register bits: bit 7 = permanent lock for pages 32-39
                if ((access & 0x80) != 0) {
                    for (int i = 32; i <= 39; i++) writablePages[i] = false;
                }
            }

        } catch (IOException e) {
            Log.e(TAG, "Error reading config", e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try { ultralight.close(); } catch (IOException ignore) {}
        }

        // Log writable pages
        StringBuilder sb = new StringBuilder("Writable pages: ");
        for (int i = 0; i < writablePages.length; i++) {
            if (writablePages[i]) sb.append(i).append(" ");
        }
        Log.d(TAG, sb.toString());

        return writablePages;
    }


    // Read NTAG213 configuration registers (AUTH0, ACCESS, PWD, PACK)
    public void readConfig(Tag tag) {
        MifareUltralight ultralight = MifareUltralight.get(tag);
        if (ultralight == null) {
            Log.e(TAG, "MifareUltralight not supported.");
            return;
        }

        try {
            ultralight.connect();
            Thread.sleep(10);
            // Page 42 (0x2A) contains AUTH0 + ACCESS
            byte[] cfg42 = ultralight.transceive(new byte[]{0x30, 42});
            if (cfg42 != null) {
                Log.d(TAG, "Page 42 (AUTH0/ACCESS): " + bytesToHex(cfg42));
                int auth0 = cfg42[0] & 0xFF;
                int access = cfg42[1] & 0xFF;
                Log.d(TAG, "AUTH0 = " + auth0 + " (first protected page)");
                Log.d(TAG, "ACCESS = " + access + " (protection flags)");
            }

            // Page 43 (0x2B) contains more config (e.g., NFC config bytes)
            byte[] cfg43 = ultralight.transceive(new byte[]{0x30, 43});
            if (cfg43 != null) {
                Log.d(TAG, "Page 43: " + bytesToHex(cfg43));
            }

            // Page 44 (0x2C) contains PWD (password)
            byte[] pwdPage = ultralight.transceive(new byte[]{0x30, 44});
            if (pwdPage != null) {
                Log.d(TAG, "Page 44 (PWD): " + bytesToHex(pwdPage));
            }

            // Page 45 (0x2D) contains PACK
            byte[] packPage = ultralight.transceive(new byte[]{0x30, 45});
            if (packPage != null) {
                Log.d(TAG, "Page 45 (PACK + RFU): " + bytesToHex(packPage));
            }

        } catch (IOException e) {
            Log.e(TAG, "Error reading config", e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try { ultralight.close(); } catch (IOException ignore) {}
        }
    }

    // Authenticate with the tag
    private boolean authenticate(MifareUltralight ultralight) throws IOException {
        try {
            byte[] response = ultralight.transceive(
                    new byte[]{
                            (byte) 0x1B, // PWD_AUTH command
                            password[0], password[1], password[2], password[3]
                    }
            );

            if (response == null || response.length < 2) {
                Log.e(TAG, "Authentication failed.");
                return false;
            }
            Log.d(TAG, "Authentication successful. PACK: " + bytesToHex(response));
            return true;
        }
        catch (Exception ex) {
            Log.e(TAG, "Authentication failed error:" + ex.getMessage());
        }

        return  false;
    }

    // Read from page (each read returns 4 pages = 16 bytes)
    public String read(Tag tag, int startPage) {
        MifareUltralight ultralight = MifareUltralight.get(tag);

        Log.e(TAG, "UID:" + Str.byteArrayToHexString(ultralight.getTag().getId()));

        if (ultralight == null) {
            Log.e(TAG, "MifareUltralight not supported.");
            return null;
        }

        try {
            ultralight.connect();
            Thread.sleep(10);
            if (!authenticate(ultralight)) return null;

            byte[] readCmd = new byte[]{(byte) 0x30, (byte) startPage};
            byte[] data = ultralight.transceive(readCmd);

            if (data != null) {
                String hexData = bytesToHex(data);
                Log.d(TAG, "Read Data: " + hexData);
                return hexData;
            }
        } catch (IOException e) {
            Log.e(TAG, "Read error", e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                ultralight.close();
            } catch (IOException ignore) {}
        }
        return null;
    }

    // Write 4 bytes to a specific page
    public boolean write(Tag tag, int page, byte[] data) {
        MifareUltralight ultralight = MifareUltralight.get(tag);
        if (ultralight == null) {
            Log.e(TAG, "MifareUltralight not supported.");
            return false;
        }

        try {
            ultralight.connect();
            Thread.sleep(10);
            if (!authenticate(ultralight)) return false;

            // byte[] data = hexStringToByteArray(hexData);

            if (data.length != 4) {
                Log.e(TAG, "Write error: NTAG213 pages require exactly 4 bytes. passed data length is ");
                return false;
            }

            byte[] cmd = new byte[6];
            cmd[0] = (byte) 0xA2; // WRITE command
            cmd[1] = (byte) page;
            System.arraycopy(data, 0, cmd, 2, 4);

            ultralight.transceive(cmd);

            Log.d(TAG, "Write success at page " + page);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Write error", e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                ultralight.close();
            } catch (IOException ignore) {}
        }
        return false;
    }

    /**
     * Safely write arbitrary-length data to NTAG213 starting at a given page.
     * Skips reserved/system pages (0-4) and only writes to user memory (pages 5-39).
     *
     * @param tag        NFC tag
     * @param startPage  starting page (recommended >=5)
     * @param data       bytes to write
     * @return true if all pages written successfully
     */
    public boolean writeBytesSafe(Tag tag, int startPage, byte[] data) {
        MifareUltralight ultralight = MifareUltralight.get(tag);
        if (ultralight == null) {
            Log.e(TAG, "MifareUltralight not supported.");
            return false;
        }

        try {
            ultralight.connect();
            Thread.sleep(10); // stabilize NFC

            // Read AUTH0 from the same connected instance
            byte auth0 = (byte) 0xFF;
            try {
                byte[] cfg42 = ultralight.transceive(new byte[]{0x30, 42});
                if (cfg42 != null && cfg42.length >= 1) auth0 = cfg42[0];
            } catch (IOException e) {
                Log.e(TAG, "Warning: Could not read AUTH0, assuming 0xFF", e);
            }

            boolean needsAuth = (auth0 <= startPage);
            if (needsAuth && !authenticate(ultralight)) {
                Log.e(TAG, "Authentication failed.");
                return false;
            }

            int page = Math.max(startPage, 5); // skip system pages 0-4
            int offset = 0;

            while (offset < data.length && page <= 39) {
                byte[] pageData = new byte[4];
                int copyLen = Math.min(4, data.length - offset);
                System.arraycopy(data, offset, pageData, 0, copyLen);

                byte[] cmd = new byte[6];
                cmd[0] = (byte) 0xA2; // WRITE
                cmd[1] = (byte) page;
                System.arraycopy(pageData, 0, cmd, 2, 4);

                boolean success = false;
                int attempts = 2;
                while (attempts-- > 0 && !success) {
                    try {
                        ultralight.transceive(cmd);
                        success = true;
                        Log.d(TAG, "Write page " + page + " OK: " + bytesToHex(pageData));
                    } catch (IOException e) {
                        if (attempts == 0) {
                            Log.e(TAG, "Failed writing page " + page, e);
                            return false;
                        }
                        Thread.sleep(20);
                    }
                }

                page++;
                offset += 4;
                Thread.sleep(10);
            }

            return true;

        } catch (IOException | InterruptedException e) {
            Log.e(TAG, "Write error", e);
            return false;
        } finally {
            try { ultralight.close(); } catch (IOException ignore) {}
        }
    }

    /**
     * Reads AUTH0 page from tag config (page 42, byte 0)
     */
    private byte readAuth0Page(Tag tag) {
        MifareUltralight ul = MifareUltralight.get(tag);
        if (ul == null) return (byte) 0xFF;

        try {
            ul.connect();
            byte[] cfg42 = ul.transceive(new byte[]{0x30, 42});
            if (cfg42 != null && cfg42.length >= 1) {
                return cfg42[0];
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading AUTH0", e);
        } finally {
            try { ul.close(); } catch (IOException ignore) {}
        }
        return (byte) 0xFF;
    }


    // Helpers
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X", b));
        return sb.toString();
    }

    /**
     * Smart write: Writes 4 bytes to a page.
     * Automatically handles authentication and protection/unprotection logic.
     *
     * protect = true  → page (and higher) become protected
     * protect = false → if page is protected, remove protection (AUTH0=0xFF)
     */
    public boolean smartWrite(Tag tag, int page, byte[] data, boolean protect) {
        if (data == null || data.length != 4) {
            Log.e(TAG, "Data must be exactly 4 bytes");
            return false;
        }

        MifareUltralight ultralight = MifareUltralight.get(tag);
        if (ultralight == null) {
            Log.e(TAG, "MifareUltralight not supported on this tag");
            return false;
        }

        try {
            ultralight.connect();
            Thread.sleep(10);

            // Read AUTH0 (page 42, byte 0)
            byte[] cfg42 = ultralight.transceive(new byte[]{0x30, 42});
            byte auth0 = (cfg42 != null && cfg42.length >= 1) ? cfg42[0] : (byte) 0xFF;

            boolean isProtected = (auth0 <= page);
            Log.d(TAG, "AUTH0=" + (auth0 & 0xFF) + " | Page " + page + " protected=" + isProtected);

            if (isProtected) {
                // Page is protected
                if (!authenticate(ultralight)) {
                    Log.e(TAG, "Authentication failed for page " + page);
                    return false;
                }

                if (!protect) {
                    // Unprotect the tag (set AUTH0 back to 0xFF)
                    Log.d(TAG, "Removing protection before writing...");
                    if (!setAuth0(ultralight, (byte) 0xFF)) {
                        Log.e(TAG, "Failed to unprotect tag");
                        return false;
                    }
                }
            }

            // Write the 4 bytes to the page
            byte[] cmd = new byte[6];
            cmd[0] = (byte) 0xA2;
            cmd[1] = (byte) page;
            System.arraycopy(data, 0, cmd, 2, 4);
            ultralight.transceive(cmd);
            Log.d(TAG, "Write OK: Page " + page + " Data: " + bytesToHex(data));

            // Optionally protect starting from this page
            if (protect && (!isProtected || auth0 != page)) {
                Log.d(TAG, "Enabling protection from page " + page);
                if (!setAuth0(ultralight, (byte) page)) {
                    Log.e(TAG, "Failed to enable protection from page " + page);
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Smart write error", e);
            return false;
        } finally {
            try { ultralight.close(); } catch (IOException ignore) {}
        }
    }

    /**
     * Updates AUTH0 so protection starts from the given page number.
     */
    private boolean setAuth0(MifareUltralight ultralight, byte pageNumber) {
        try {
            // Must be authenticated to change config
            if (!authenticate(ultralight)) {
                Log.e(TAG, "Cannot set AUTH0, authentication failed.");
                return false;
            }

            // Read current config
            byte[] cfg42 = ultralight.transceive(new byte[]{0x30, 42});
            if (cfg42 == null || cfg42.length < 4) {
                Log.e(TAG, "Failed to read config for AUTH0 update.");
                return false;
            }

            // Replace byte 0 (AUTH0)
            cfg42[0] = pageNumber;

            // Write back the 4 bytes to page 42
            byte[] cmd = new byte[6];
            cmd[0] = (byte) 0xA2;
            cmd[1] = (byte) 42;
            System.arraycopy(cfg42, 0, cmd, 2, 4);
            ultralight.transceive(cmd);

            Log.d(TAG, "AUTH0 updated: protection now starts at page " + (pageNumber & 0xFF));
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error updating AUTH0", e);
            return false;
        }
    }

    /**
     * Reads a page (4 bytes) from NTAG213 without using any password.
     * Returns null if the page is protected or read fails.
     */
    public byte[] readWithoutPassword(Tag tag, int page) {
        MifareUltralight ultralight = MifareUltralight.get(tag);
        if (ultralight == null) {
            Log.e(TAG, "MifareUltralight not supported on this tag");
            return null;
        }

        try {
            ultralight.connect();
            Thread.sleep(10);

            // Attempt to read the page (READ = 0x30)
            byte[] response = ultralight.transceive(new byte[]{(byte) 0x30, (byte) page});

            if (response != null && response.length >= 4) {
                byte[] data = Arrays.copyOfRange(response, 0, 4);
                Log.d(TAG, "Read OK (no password): Page " + page + " = " + bytesToHex(data));
                return data;
            } else {
                Log.e(TAG, "Invalid response while reading page " + page);
                return null;
            }

        } catch (IOException e) {
            // Happens if page is protected or out of range
            Log.e(TAG, "Read failed (possibly protected): Page " + page, e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error while reading page " + page, e);
            return null;
        } finally {
            try { ultralight.close(); } catch (IOException ignore) {}
        }
    }

    /**
     * Permanently lock a page (make it read-only).
     * WARNING: This cannot be undone!
     *
     * @param tag  NFC tag
     * @param page Page number (4–39)
     * @return true if successfully locked
     */
    public boolean lockPage(Tag tag, int page) {
        if (page < 4 || page > 39) {
            Log.e(TAG, "Invalid page number: " + page);
            return false;
        }

        MifareUltralight ul = MifareUltralight.get(tag);
        if (ul == null) {
            Log.e(TAG, "MifareUltralight not supported.");
            return false;
        }

        try {
            ul.connect();
            Thread.sleep(10);

            // Read static and dynamic lock bytes
            byte[] lockBytesPage2 = ul.transceive(new byte[]{0x30, 2}); // static lock bytes
            byte[] lockBytesPage40 = ul.transceive(new byte[]{0x30, 40}); // dynamic lock bytes

            if (lockBytesPage2 == null || lockBytesPage40 == null) {
                Log.e(TAG, "Failed to read lock bytes.");
                return false;
            }

            // Dynamic lock bits (in page 40)
            // Each bit corresponds to a range of user memory pages
            int bitIndex = (page - 4);
            int byteIndex = bitIndex / 8;
            int bitPos = bitIndex % 8;

            lockBytesPage40[byteIndex] |= (1 << bitPos); // set lock bit

            // Write updated lock bytes back to page 40
            byte[] cmd = new byte[6];
            cmd[0] = (byte) 0xA2; // WRITE
            cmd[1] = (byte) 40;   // dynamic lock page
            System.arraycopy(lockBytesPage40, 0, cmd, 2, 4);
            ul.transceive(cmd);

            Log.i(TAG, "Page " + page + " permanently locked.");
            return true;

        } catch (IOException e) {
            Log.e(TAG, "Locking failed for page " + page, e);
            return false;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try { ul.close(); } catch (IOException ignore) {}
        }
    }

    /**
     * Changes the NTAG213 password (PWD) and optionally the PACK.
     *
     * @param tag            NFC Tag
     * @param newPasswordHex New 4-byte password (8 hex characters, e.g. "A1B2C3D4")
     * @param newPackHex     Optional new PACK (4 hex characters, e.g. "1234") – can be null
     * @return true if password successfully changed
     */
    public boolean changePassword(Tag tag, String newPasswordHex, String newPackHex) {
        MifareUltralight ul = MifareUltralight.get(tag);
        if (ul == null) {
            Log.e(TAG, "MifareUltralight not supported.");
            return false;
        }

        try {
            ul.connect();
            Thread.sleep(10);

            // Authenticate with current password before changing
            if (!authenticate(ul)) {
                Log.e(TAG, "Authentication failed — cannot change password.");
                return false;
            }

            // Convert password to bytes
            byte[] newPwd = hexStringToByteArray(newPasswordHex);
            if (newPwd.length != 4) {
                Log.e(TAG, "Invalid password length — must be 4 bytes (8 hex chars).");
                return false;
            }

            // Write new password to page 44 (0x2C)
            byte[] writePwdCmd = new byte[6];
            writePwdCmd[0] = (byte) 0xA2; // WRITE command
            writePwdCmd[1] = (byte) 44;   // Page 44
            System.arraycopy(newPwd, 0, writePwdCmd, 2, 4);
            ul.transceive(writePwdCmd);

            Log.i(TAG, "Password updated successfully (page 44).");

            // If PACK provided, write to page 45 (0x2D)
            if (newPackHex != null && newPackHex.length() == 4) {
                byte[] newPack = hexStringToByteArray(newPackHex);
                byte[] writePackCmd = new byte[6];
                writePackCmd[0] = (byte) 0xA2;
                writePackCmd[1] = (byte) 45; // Page 45
                System.arraycopy(newPack, 0, writePackCmd, 2, 2); // PACK = 2 bytes
                writePackCmd[4] = 0x00; // RFU (Reserved)
                writePackCmd[5] = 0x00;
                ul.transceive(writePackCmd);

                Log.i(TAG, "PACK updated successfully (page 45).");
            }

            // Update this instance’s stored password to the new one
            this.password = newPwd;

            return true;

        } catch (IOException e) {
            Log.e(TAG, "Failed to change password", e);
            return false;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try { ul.close(); } catch (IOException ignore) {}
        }
    }



}

