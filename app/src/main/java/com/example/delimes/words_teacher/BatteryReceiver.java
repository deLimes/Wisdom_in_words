package com.example.delimes.words_teacher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.widget.Toast;

public class BatteryReceiver extends BroadcastReceiver {

    boolean powerConnected = false;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
            powerConnected = true;
        } else {
            intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED);
            powerConnected = false;
        }
        if (powerConnected && intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
            Integer batteryLevel = intent.getIntExtra("level", 0);
            if (batteryLevel.equals(100)){
                Collocation collocation2 = new Collocation(false,"",false,"",false,0);
                TService.sendNotify("Battery level: "+batteryLevel+"%",collocation2);
            }
        }
    }
}
