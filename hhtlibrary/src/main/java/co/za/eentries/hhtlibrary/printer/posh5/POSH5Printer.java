package co.za.eentries.hhtlibrary.printer.posh5;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;

import net.nyx.printerservice.print.IPrinterService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import co.za.eentries.hhtlibrary.printer.IPrinter;
import co.za.eentries.hhtlibrary.utility.Common;
import recieptservice.com.recieptservice.PrinterInterface;

public class POSH5Printer implements IPrinter {

    protected PrinterInterface printerInterface;
    protected Context context;
    protected boolean isServiceConnected;
    protected ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    protected Handler handler= new Handler();

    public POSH5Printer(Context sourceContext){

        context = sourceContext;
        Common.log("POSH5Printer|Connecting... ");
        bindService();

    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {

            Common.log("POSH5Printer: Disconnected");
            isServiceConnected = false;
            printerInterface = null;
            handler.postDelayed(() -> bindService(), 5000);

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {


            Common.log("POSH5Printer: Connected");
            printerInterface = PrinterInterface.Stub.asInterface(service);
            isServiceConnected = true;

//            printText("Testing");
//            printBarCode("12345678", 162, 2);
//            printQRCode("12345678", 3, 0);

        }
    };

    public  void bindService() {
        Intent intent = new Intent();
        intent.setClassName("recieptservice.com.recieptservice", "recieptservice.com.recieptservice.service.PrinterService");
        context.startService(intent);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindService() {
        context.unbindService(serviceConnection);
    }

    @Override
    public void printText(String text) {

        if(printerInterface != null) {

            try {
                if(text == null || text.isEmpty())
                    Common.log("POSH5Printer.printTex|error[0]: text is null or empty");
                else {
                    printerInterface.printText(text);
                    printNewline(1);
                }

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
    public void printBarCode(String data, int height, int width) {

        if(printerInterface != null) {

            try {
                if(data == null || data.isEmpty())
                    Common.log("POSH5Printer.printBarCode|error[0]: data is null or empty");
                else
                    printerInterface.print128BarCode(data, 3, height, width);

            } catch (Exception ex) {
                Common.log("POSH5Printer.printBarCode|error[1]:" + ex.getMessage());
            }

        }
        else
            Common.log("POSH5Printer.printBarCode|error[2]:service is not ready yet");

    }

    @Override
    public void printQRCode(String data, int height, int width) {

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
    public void printNewline(int numberOfLines) {

        if(printerInterface != null) {

            try {

                printerInterface.nextLine(numberOfLines);

            } catch (Exception ex) {
                Common.log("POSH5Printer.printNewline|error[1]:" + ex.getMessage());
            }

        }
        else
            Common.log("POSH5Printer.printNewline|error[2]:service is not ready yet");

    }

    @Override
    public boolean isConnected() {
        return isServiceConnected;
    }
}
