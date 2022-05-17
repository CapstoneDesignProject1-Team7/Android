package com.example.android;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

public class WifiConnectionService extends Service {
    WifiConnectionCheck wifiConnectionCheck;
    public WifiConnectionService() {  }
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(Constants.ACTION_START_WIFI_SERVICE)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {    //  LOLLIPOP Version 이상..
                        if(wifiConnectionCheck==null){
                            wifiConnectionCheck=new WifiConnectionCheck(getApplicationContext());
                            wifiConnectionCheck.register();
                        }
                    }
                } else if (action.equals(Constants.ACTION_STOP_WIFI_SERVICE)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {     //  LOLLIPOP Version 이상..
                        if(wifiConnectionCheck!=null) wifiConnectionCheck.unregister();
                    }
                    stopForeground(true);
                    stopSelf();
                }
            }
        }
        return START_STICKY;    // START_STICKY : 시스템에 의해 종료 되어도 다시 생성 시켜주는 것
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}