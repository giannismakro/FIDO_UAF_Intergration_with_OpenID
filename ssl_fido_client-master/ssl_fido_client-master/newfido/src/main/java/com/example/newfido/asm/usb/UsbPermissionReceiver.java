package com.example.newfido.asm.usb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

/**
 * Created by sorin.teican on 13-Jan-17.
 */
 

public class UsbPermissionReceiver extends BroadcastReceiver {

    public static final String ACTION_USB_PERMISSION = "com.example.newfido.USB_PERMISSION";

    private UsbDevice mUsbDevice = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (ACTION_USB_PERMISSION.equals(action)) {
            synchronized (this) {
                synchronized (mUsbDevice) {
                    mUsbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);


                }
            }
        }
    }
}
