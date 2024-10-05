package co.za.eentries.hhtlibrary.printer;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.RemoteException;

import com.sr.SrPrinter;

import co.za.eentries.hhtlibrary.utility.Common;
import co.za.eentries.hhtlibrary.utility.Device;
import recieptservice.com.recieptservice.PrinterInterface;

class POSH5Printer implements IPrinter {

    protected PrinterInterface printerInterface;
    protected Context context;
    protected boolean isServiceConnected;

    public POSH5Printer(Context sourceContext){

        context = sourceContext;

        Common.log("POSH5Printer|Connecting... ");
//        Common.log("Model: " + Device.getModel());
//        Common.log("Manufacture: " + Device.getManufacture());

        SrPrinter.bindPrinter(context, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

                isServiceConnected = true;

                Common.log("POSH5Printer: Connected");

                printerInterface = PrinterInterface.Stub.asInterface(iBinder);

                printText("Testing");
                printBarCode("12345678");
                printQRCode("12345678");

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

                isServiceConnected = false;
                Common.log("POSH5Printer: Disconnected");
            }
        });

    }

    @Override
    public void printText(String text) {

        if(printerInterface != null) {

            try {
                if(text == null || text.isEmpty())
                    Common.log("POSH5Printer.printTex|error[0]: text is null or empty");
                else
                    printerInterface.printText(text);

            } catch (Exception ex) {
                Common.log("POSH5Printer.printTex|error[1]:" + ex.getMessage());
            }

        }
    }

    @Override
    public void printBitmap(Bitmap bitmap) {

        if(printerInterface != null) {

            try {
                if(bitmap == null)
                    Common.log("POSH5Printer.printBitmap|error[0]: text is null or empty");
                else
                    printerInterface.printBitmap(bitmap);

            } catch (Exception ex) {
                Common.log("POSH5Printer.printBitmap|error[1]:" + ex.getMessage());
            }

        }
        else
            Common.log("POSH5Printer.printBitmap|error[2]:service is not ready yet");
    }

    @Override
    public void printBarCode(String data) {

        if(printerInterface != null) {

            try {
                if(data == null || data.isEmpty())
                    Common.log("POSH5Printer.printBarCode|error[0]: data is null or empty");
                else
                    printerInterface.print128BarCode(data, 3, 80, 2);

            } catch (Exception ex) {
                Common.log("POSH5Printer.printBarCode|error[1]:" + ex.getMessage());
            }

        }
        else
            Common.log("POSH5Printer.printBarCode|error[2]:service is not ready yet");

    }

    @Override
    public void printQRCode(String data) {

        if(printerInterface != null) {

            try {
                if(data == null || data.isEmpty())
                    Common.log("POSH5Printer.printQRCode|error[0]: data is null or empty");
                else
                    printerInterface.printQRCode(data, 4,3);

            } catch (Exception ex) {
                Common.log("POSH5Printer.printQRCode|error[1]:" + ex.getMessage());
            }

        }
        else
            Common.log("POSH5Printer.printQRCode|error[2]:service is not ready yet");

    }

    @Override
    public boolean isConnected() {
        return isServiceConnected;
    }
}
