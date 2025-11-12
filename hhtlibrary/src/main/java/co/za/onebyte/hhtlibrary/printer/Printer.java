package co.za.onebyte.hhtlibrary.printer;
import android.content.Context;

import co.za.onebyte.hhtlibrary.printer.nyx.NYXPrinter;
import co.za.onebyte.hhtlibrary.printer.posh5.POSH5Printer;
import co.za.onebyte.hhtlibrary.utility.Common;
import co.za.onebyte.hhtlibrary.utility.Device;
import co.za.onebyte.hhtlibrary.utility.DeviceModelType;


public class Printer {

    private static IPrinter printerSingletonInstance;

    public static IPrinter instance(Context context){

        Common.log("SerialNumber: " + Device.getSerialNumber());
        Common.log("Model: " + Device.getModel());
        Common.log("Manufacture: " + Device.getManufacture());

        if(printerSingletonInstance == null) {

            if (Device.getModelType() == DeviceModelType.AlpsPOSH5)
                printerSingletonInstance = new POSH5Printer(context);
            if (Device.getModelType() == DeviceModelType.NyxNB55)
                printerSingletonInstance = new NYXPrinter(context);
        }

        return  printerSingletonInstance;

    }
}
