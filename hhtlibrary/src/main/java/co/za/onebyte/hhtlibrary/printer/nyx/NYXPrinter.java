package co.za.onebyte.hhtlibrary.printer.nyx;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;

import net.nyx.printerservice.print.IPrinterService;
import net.nyx.printerservice.print.PrintTextFormat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import co.za.onebyte.hhtlibrary.printer.IPrinter;
import co.za.onebyte.hhtlibrary.utility.Common;

public class NYXPrinter implements IPrinter {

    protected IPrinterService printerInterface;
    protected Context context;
    protected boolean isServiceConnected;
    protected ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    protected Handler handler= new Handler();

    public NYXPrinter(Context sourceContext){

        context = sourceContext;
        Common.log("NYXPrinter|Connecting... ");
        bindService();

    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {

            Common.log("NYXPrinter: Disconnected");
            isServiceConnected = false;
            printerInterface = null;
            handler.postDelayed(() -> bindService(), 5000);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            Common.log("NYXPrinter: Connected");

            isServiceConnected = true;
            printerInterface = IPrinterService.Stub.asInterface(service);

           // printText("Testing...");

        }
    };

    private void bindService() {

        Intent intent = new Intent();
        intent.setPackage("net.nyx.printerservice");
        intent.setAction("net.nyx.printerservice.IPrinterService");
        context.startService(intent);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public void printText(String text) {

        if(printerInterface != null) {

            if(text == null || text.isEmpty())
                Common.log("POSH5Printer.printTex|error[0]: text is null or empty");
            else {

                singleThreadExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            PrintTextFormat textFormat = new PrintTextFormat();
                            // textFormat.setTextSize(32);
                            // textFormat.setUnderline(true);
                            int ret = printerInterface.printText(text, textFormat);

                            if (ret == 0)
                                printerInterface.printEndAutoOut();

                        } catch (Exception ex) {
                            Common.log("POSH5Printer.printTex|error[1]:" + ex.getMessage());
                        }
                    }
                });

            }

        }
    }

    @Override
    public void printBitmap(Bitmap bitmap) {

        if(printerInterface != null) {

            try {
                if(bitmap == null)
                    Common.log("POSH5Printer.printBitmap|error[0]: text is null or empty");
                else{}
                    //printerInterface.printBitmap(bitmap);

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
                else {
                    //printerInterface.print128BarCode(data, 3, 80, 2);
                }

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
                else{

                }
                    //printerInterface.printQRCode(data, 4,3);

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
               // printerInterface.(data, 3, 80, 2);

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
