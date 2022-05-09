package com.example.android;

import static android.content.Context.BATTERY_SERVICE;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

public class BatteryLevelReceiver extends BroadcastReceiver {
    int batteryLevel;
    BatteryLevelReceiver(){ }
    BatteryLevelReceiver(Context context){
        BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
        batteryLevel =  bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        if (batteryLevel > 15) Constants.NETWORK_FREQUENCY = 3000;
        else Constants.NETWORK_FREQUENCY = 6000;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        if (intentAction != null){
            if (Intent.ACTION_BATTERY_LOW.equals(intentAction)) {
                Constants.NETWORK_FREQUENCY = 6000;
            }
        }
    }
}
