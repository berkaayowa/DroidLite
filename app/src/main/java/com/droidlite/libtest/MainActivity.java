package com.droidlite.libtest;

import static co.za.onebyte.hhtlibrary.utility.Str.hexStringToByteArray;
import static co.za.onebyte.hhtlibrary.utility.Str.stringToByteArray;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.droidlite.sqlite.DroidLiteSetup;
import com.droidlite.sqlite.common.Helper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import co.za.onebyte.hhtlibrary.printer.IPrinter;
import co.za.onebyte.hhtlibrary.printer.Printer;
import co.za.onebyte.hhtlibrary.scanner.CameraScanner;
import co.za.onebyte.hhtlibrary.scanner.NfcUniversal;
import co.za.onebyte.hhtlibrary.scanner.ScannerRequest;
import co.za.onebyte.hhtlibrary.utility.Common;
import co.za.onebyte.hhtlibrary.utility.Str;
import universal.nfc.NTAG213;

public class MainActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private Tag lastTag; //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DroidLiteSetup.dataBase(this, "tets.db",
                new Class[]{
                        User.class
                }, true);

        //Printer.instance(this);

       // myPrinter.printBarCode("berka testing");

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC not supported on this device", Toast.LENGTH_LONG).show();
        }else {

            // Prepare Foreground Dispatch
            pendingIntent = PendingIntent.getActivity(
                    this, 0,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    PendingIntent.FLAG_MUTABLE // Required for Android 12+
            );

            IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
            intentFiltersArray = new IntentFilter[]{ndef};


        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            lastTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (lastTag != null) {
                Toast.makeText(this, "Tag detected! Ready for Read/Write.", Toast.LENGTH_SHORT).show();

                //readNFC();
                //scanNFC();
                //writeNFC();
                //testNTAG();
                test();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    private void scanNFC() {

        NfcUniversal helper = new NfcUniversal(lastTag);
        String uid = helper.getUid();
        String type = helper.getTagType();

        Common.log("NFC UID: " + uid);
        Common.log("NFC Type: " + type);
        Common.log("NFC Techs: " + Arrays.toString(helper.getSupportedTechs()));

        // Example: Try reading Classic block
        byte[] key = hexStringToByteArray("A7F2D39C8B1E45A0C3D8F79E6A24BC17");
        Common.log("key: " + Str.byteArrayToHexString(key));

        //byte[] block = helper.readMifareClassicBlock(1, 4, key, true);
        //byte[] block = helper.readMifareUltralightPage(4);

        byte[] block = helper.readMifareUltralightPage(4, key, true);

        if (block != null) {
            Common.log("NFC Read Block: " + Arrays.toString(block));
            Toast.makeText(MainActivity.this, "Read OK, UID=" + uid, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Read failed (maybe not MIFARE Classic)", Toast.LENGTH_SHORT).show();
        }

    }
    private void testNTAG2() {

        // Create NfcUniversal instance
        co.za.onebyte.hhtlibrary.scanner.nfc.NfcUniversal nfc = new co.za.onebyte.hhtlibrary.scanner.nfc.NfcUniversal(lastTag);

        String uid = nfc.getUid();
        String type = nfc.getTagType();

        Common.log("NFC UID: " + uid);
        Common.log("NFC Type: " + type);
        Common.log("NFC Techs: " + Arrays.toString(nfc.getSupportedTechs()));

// Check if it's an NTAG
        if (nfc.isNtag()) {
            String ntagType = nfc.getNtagType();
            Common.log("Detected: " + ntagType);

            // Read with password (null for no password)
            byte[] password = hexStringToByteArray("A7F2D39C8B1E45A0C3D8F79E6A24BC17");
            //byte[] password = new byte[]{0x01, 0x02, 0x03, 0x04};
            byte[] pageData = nfc.readNtagPage(4, password);

            // Write with password
//            byte[] data = new byte[]{0x48, 0x65, 0x6C, 0x6C}; // "Hell"
//            boolean success = nfc.writeNtagPage(5, data, password);

            // Read NDEF content
           // String ndefContent = nfc.readNtagNdef(password);

            // Write NDEF text
            //nfc.writeNtagNdefText("Hello, NTAG!", password);

            // Set new password
//            byte[] newPassword = new byte[]{0x0A, 0x0B, 0x0C, 0x0D};
//            byte[] pack = new byte[]{(byte) 0xE1, (byte) 0xF2};
//            nfc.setNtagPassword(newPassword, pack);
        }

    }

    private void testNTAG3() {

        // Create NfcUniversal instance
        co.za.onebyte.hhtlibrary.scanner.nfc.NfcUniversal nfc = new co.za.onebyte.hhtlibrary.scanner.nfc.NfcUniversal(lastTag);

        String uid = nfc.getUid();
        String type = nfc.getTagType();

        Common.log("NFC UID: " + uid);
        Common.log("NFC Type: " + type);
        Common.log("NFC Techs: " + Arrays.toString(nfc.getSupportedTechs()));

        /// Example usage for different NTAG types
        String ntagType = nfc.getNtagType();

        if (ntagType.startsWith("NTAG")) {
            // For regular NTAG tags (213, 215, 216)
            if (!ntagType.contains("424")) {
                byte[] password = new byte[]{0x01, 0x02, 0x03, 0x04}; // 4-byte password

                // Read with password
                byte[] pageData = nfc.readNtagPage(4, password);

                if (pageData != null) {
                    Common.log("Page 4 data: " + Str.byteArrayToHexString(pageData));
                }

                // Write with password
//                byte[] writeData = new byte[]{0x48, 0x65, 0x6C, 0x6C}; // "Hell"
//                boolean success = nfc.writeNtagPage(5, writeData, password);
//                Common.log("Write " + (success ? "successful" : "failed"));
            }
            // For NTAG424 DNA
            else {
                byte[] aesKey = new byte[16]; // 16-byte AES key

                // Read with AES encryption
                byte[] pageData = nfc.readNtagPage(4, aesKey);
                if (pageData != null) {
                    Common.log("Page 4 data: " + Str.byteArrayToHexString(pageData));
                }

                // Write with AES encryption
//                byte[] writeData = new byte[]{0x48, 0x65, 0x6C, 0x6C}; // "Hell"
//                boolean success = nfc.writeNtagPage(5, writeData, aesKey);
//                Common.log("Write " + (success ? "successful" : "failed"));
            }
        }

    }

    private void testNTAG() {

        // Create NfcUniversal instance
        co.za.onebyte.hhtlibrary.scanner.nfc.NfcUniversal nfc = new co.za.onebyte.hhtlibrary.scanner.nfc.NfcUniversal(lastTag);

        Common.log("NFC UID: " + nfc.getUid());
        Common.log("NFC Type: " + nfc.getTagType());
        Common.log("IsoMode: " + nfc.isIsoMode());
        Common.log("NFC Techs: " + Arrays.toString(nfc.getSupportedTechs()));

// Handle different tag types
        String tagType = nfc.getTagType();

        switch (tagType) {
            case "NTAG424 DNA (ISO Mode)":
                Common.log("Handling NTAG424 DNA tag");
                // Use NTAG424 DNA specific methods
                byte[] aesKey = hexStringToByteArray("3F9A7C1D"); // Your AES key
                byte[] pageData = nfc.readNtagPage(4, aesKey);

                if (pageData != null) {
                    Common.log("Page 4 data: " + Str.byteArrayToHexString(pageData));
                }

                break;

            case "MIFARE Classic":
                Common.log("Handling MIFARE Classic tag");
                // Use MIFARE Classic methods
                byte[] defaultKey = new byte[6]; // Default key
                byte[] blockData = nfc.readMifareClassicBlock(0, 0, defaultKey, true);
                break;

            case "MIFARE Ultralight":
                Common.log("Handling MIFARE Ultralight tag");
                // Use MIFARE Ultralight methods
                byte[] key = hexStringToByteArray("A7F2D39C8B1E45A0C3D8F79E6A24BC17"); // Your AES key
                byte[] blockDatax = nfc.readMifareUltralightPage(0, key, true);

                if (blockDatax != null) {
                    Common.log("Data: " + Str.byteArrayToHexString(blockDatax));
                }

                break;

            case "DESFire / ISO-DEP":
                Common.log("Handling DESFire tag");
                // Use DESFire methods
                byte[] command = new byte[]{(byte) 0x90, (byte) 0x60, 0x00, 0x00, 0x00};
                byte[] response = nfc.sendDesfireCommand(command);
                break;

            case "NDEF":
                Common.log("Handling NDEF tag");
                // Use NDEF methods
                String ndefContent = nfc.readNdef();
                break;

            default:
                Common.log("Handling generic tag type: " + tagType);
                break;
        }


    }

    private void test() {
        //readProtectedNtag213(lastTag);
        //readNtag424Data();
        //Common.log("Response|" + readFromNtag424(lastTag, 4, 16));


        readAndWriteNtag213(lastTag);

    }


    private void readAndWriteNtag213(Tag tag) {

        int startPage = 5;
        NTAG213 ntag213 = new NTAG213("FFFFFFFF");
        //Ntag213 ntag213 = new Ntag213("3F9A7C1D");
       // ntag213.readConfig(tag);
        byte[] newData = {(byte)'P', (byte)'2', (byte)'4', (byte)' '};

//        if(ntag213.smartWrite(tag, startPage, newData, false)) {
//            Common.log("Tag.write:" + byteArrayToHexString(newData));
//        }
//        else
//            Common.log("Tag.write.Error:" + byteArrayToHexString(newData));
//        boolean[] xx = ntag213.getWritablePages(tag);
//
//        ntag213.dumpWritablePages(xx);
        Common.log("Tag.Read: "  + ntag213.readWithoutPassword(tag, startPage));
        //ntag.write(tag, 6, new byte[]{0x11, 0x22, 0x33, 0x44});
//        if(ntag213.writeBytesSafe(tag, startPage, new byte[]{0x11, 0x22, 0x33, 0x44})) {
//            Common.log("ReadUpdated");
//        }
//        else {
//            Common.log("Could not write");
//        }

    }

    public void qrCodeScan(View view) {

        Intent intent = new Intent(this, CameraScanner.class);                                         // Added by Chris
        startActivityForResult(intent, ScannerRequest.BarCode);

    }

    public void printText(View view){

        IPrinter myPrinter = Printer.instance(this);
        myPrinter.printText("printing something");

    }

    private void testDroidLite() {

        User user = new User();
        user.Id = 0;
        user.Name = "ferre";
        //user.Dob = new Date();
        user.Salary = 4;
        user.HourWorked = 6;

        if(user.save()) {
            Helper.log("Saved");
        }
        else {
            Helper.log("Could not be save");
        }

//        ArrayList<IEntity> list = User.getAll();
//
//        if(!list.isEmpty()) {
//
//            User testuser = (User) list.get(0);
//
//            Helper.log("Data size:" + String.valueOf(list.size()));
//            Helper.log("Has data:" + String.valueOf(testuser.Id));
//
//            testuser.Name = "omba";
//
//            if(testuser.save())
//                Helper.log("Updated");
//
//            if(list.size() > 1) {
//
//                testuser = (User) list.get(1);
//
//                if(testuser.delete()) {
//                    Helper.log("Deleted");
//                }
//            }
//        }
//        else {
//            Helper.log("Could not be save");
//         }

//        ArrayList<IEntity> list = User.getAll();
//        User muser = new User(1);
//        muser.delete();

    }
}