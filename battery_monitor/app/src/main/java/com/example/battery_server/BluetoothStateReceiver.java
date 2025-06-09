package com.example.battery_server;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BluetoothStateReceiver extends BroadcastReceiver {
    
    private MainActivity mainActivity;
    
    public BluetoothStateReceiver() {
        // 默认构造函数，用于系统注册
    }
    
    public BluetoothStateReceiver(MainActivity activity) {
        this.mainActivity = activity;
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            
            if (mainActivity != null) {
                mainActivity.onBluetoothStateChanged(state);
            }
        }
    }
} 