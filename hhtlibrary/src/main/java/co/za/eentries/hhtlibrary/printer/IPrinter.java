package co.za.eentries.hhtlibrary.printer;

import android.graphics.Bitmap;

public interface IPrinter {
    void printText(String text);
    void printBitmap(Bitmap bitmap);
    void printBarCode(String data);
    void printQRCode(String data);
    boolean isConnected();
}
