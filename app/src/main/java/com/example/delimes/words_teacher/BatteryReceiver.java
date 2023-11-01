package com.example.delimes.words_teacher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.widget.Toast;

public class BatteryReceiver extends BroadcastReceiver {

    public boolean powerConnected = false;
    //public Integer batteryLevel = 0;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
            Toast.makeText(context, "The device is charging", Toast.LENGTH_SHORT).show();
            powerConnected = true;
        }
        if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
            Toast.makeText(context, "The device is not charging", Toast.LENGTH_SHORT).show();
            powerConnected = false;
        }

        if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
            Integer batteryLevel = intent.getIntExtra("level", 0);
            if (powerConnected && batteryLevel.equals(100)){
                Collocation collocation2 = new Collocation(false,"",false,"",false,0);
                TService.sendNotify("Battery level: "+batteryLevel+"%",collocation2);
            }
        }
    }
}
