package co.za.eentries.hhtlibrary.printer;

import android.graphics.Bitmap;

public interface IPrinter {
    void printText(String text);
    void printBitmap(Bitmap bitmap);
    void printBarCode(String data, int height, int width);
    void printQRCode(String data, int height, int width);
    void printNewline(int numberOfLines);
    boolean isConnected();
}
