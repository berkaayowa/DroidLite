package co.za.onebyte.hhtlibrary.scanner.nfc;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;

import java.io.IOException;

import co.za.onebyte.hhtlibrary.utility.Common;

public   class MifareClassicHandler {
    private final Tag tag;

    MifareClassicHandler(Tag tag) {
        this.tag = tag;
    }

    byte[] readBlock(int sectorIndex, int blockIndex, byte[] key, boolean useKeyA) {
        MifareClassic mfc = MifareClassic.get(tag);
        if (mfc == null) {
            Common.log("MIFARE Classic tag is null");
            return null;
        }

        try {
            mfc.connect();
            if (key == null) {
                key = MifareClassic.KEY_DEFAULT;
            }

            boolean auth = useKeyA
                    ? mfc.authenticateSectorWithKeyA(sectorIndex, key)
                    : mfc.authenticateSectorWithKeyB(sectorIndex, key);

            if (auth) {
                return mfc.readBlock(blockIndex);
            } else {
                Common.log("MIFARE Classic authentication failed");
            }
        } catch (IOException ex) {
            Common.log("MIFARE Classic error: " + ex.getMessage());
        } finally {
            try {
                if (mfc != null && mfc.isConnected()) {
                    mfc.close();
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    boolean writeBlock(int sectorIndex, int blockIndex, byte[] data, byte[] key, boolean useKeyA) {
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
            Common.log("MIFARE Classic write error: " + e.getMessage());
        } finally {
            try {
                if (mfc != null && mfc.isConnected()) {
                    mfc.close();
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }
}