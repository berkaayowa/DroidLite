package co.za.onebyte.hhtlibrary.scanner;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import co.za.onebyte.hhtlibrary.utility.Common;
import co.za.onebyte.hhtlibrary.utility.Device;
import co.za.onebyte.hhtlibrary.utility.DeviceModelType;

public class CameraScanner extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Device.getModelType() == DeviceModelType.NyxNB55)
            nyxScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Intent intent = new Intent();

        if (requestCode == ScannerRequest.BarCodeOrQrCode && resultCode == RESULT_OK && data != null) {
            String result = data.getStringExtra("SCAN_RESULT");
            Common.log("Scanner result: " + result);
            intent.putExtra("SCAN_RESULT", result);

        }

        setEndResult(intent);
    }

    private void nyxScan() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("net.nyx.scanner", "net.nyx.scanner.ScannerActivity"));
        // set the capture activity actionbar title
        //intent.putExtra("TITLE", "Scan");
        // show album icon, default true
        // intent.putExtra("SHOW_ALBUM", true);
        // play beep sound when get the scan result, default true
        // intent.putExtra("PLAY_SOUND", true);
        // play vibrate when get the scan result, default true
        // intent.putExtra("PLAY_VIBRATE", true);
        startActivityForResult(intent, ScannerRequest.BarCodeOrQrCode);

    }

    private void setEndResult(Intent intent) {

        this.setResult(RESULT_OK, intent);
        finish();

    }


}