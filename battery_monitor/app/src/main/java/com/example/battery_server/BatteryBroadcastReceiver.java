package com.example.battery_server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BatteryBroadcastReceiver extends BroadcastReceiver {
    
    private MainActivity mainActivity;
    
    public BatteryBroadcastReceiver(MainActivity activity) {
        this.mainActivity = activity;
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
            if (mainActivity != null) {
                mainActivity.onBatteryChanged();
            }
        }
    }
} 