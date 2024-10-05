package co.za.eentries.hhtlibrary.printer;
import android.content.Context;

import co.za.eentries.hhtlibrary.utility.Common;
import co.za.eentries.hhtlibrary.utility.Device;
import co.za.eentries.hhtlibrary.utility.DeviceModelType;


public class Printer {

    private static IPrinter printerSingletonInstance;

    public static IPrinter instance(Context context){

        Common.log("SerialNumber: " + Device.getSerialNumber());
        Common.log("Model: " + Device.getModel());
        Common.log("Manufacture: " + Device.getManufacture());

        if(printerSingletonInstance == null) {

            if (Device.getModelType() == DeviceModelType.AlpsPOSH5)
                printerSingletonInstance = new POSH5Printer(context);
        }

        return  printerSingletonInstance;

    }
}
