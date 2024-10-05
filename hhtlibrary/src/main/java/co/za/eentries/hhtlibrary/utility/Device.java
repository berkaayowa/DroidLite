package co.za.eentries.hhtlibrary.utility;

import android.os.Build;

public class Device {

    public static String getModel() {

        String model = Build.MODEL;

        return  model;

    }

    public static String getManufacture() {

        String manufacturer = Build.MANUFACTURER;

        return  manufacturer;

    }

    public static String getSerialNumber() {

        String serialNumber = "";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            serialNumber = Build.getSerial();
        }
        else {
            serialNumber = Build.SERIAL;
        }

        return  serialNumber;

    }

    public static DeviceModelType getModelType() {

        String model = getModel();

        if(model.equalsIgnoreCase("POSH5-OS01"))
            return DeviceModelType.AlpsPOSH5;
        if(model.equalsIgnoreCase("NB55"))
            return DeviceModelType.NyxNB55;

        return DeviceModelType.Unknown;

    }


}
