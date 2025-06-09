package com.example.battery_server;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;

public class BluetoothManager {
    
    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    
    public BluetoothManager(Context context) {
        this.context = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    
    /**
     * 检查设备是否支持蓝牙
     */
    public boolean isBluetoothSupported() {
        return bluetoothAdapter != null;
    }
    
    /**
     * 检查蓝牙是否已启用
     */
    public boolean isBluetoothEnabled() {
        if (!isBluetoothSupported()) {
            return false;
        }
        
        // 检查权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(context, 
                    android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return false; // 没有权限，无法检查状态
            }
        }
        
        return bluetoothAdapter.isEnabled();
    }
    
    /**
     * 获取蓝牙状态文本
     */
    public String getBluetoothStatusText() {
        if (!isBluetoothSupported()) {
            return "不支持蓝牙";
        }
        
        // 检查权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(context, 
                    android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return "缺少蓝牙权限";
            }
        }
        
        int state = bluetoothAdapter.getState();
        return getBluetoothStateText(state);
    }
    
    /**
     * 根据状态码获取状态文本
     */
    public String getBluetoothStateText(int state) {
        switch (state) {
            case BluetoothAdapter.STATE_OFF:
                return "已关闭";
            case BluetoothAdapter.STATE_ON:
                return "已开启";
            case BluetoothAdapter.STATE_TURNING_OFF:
                return "正在关闭";
            case BluetoothAdapter.STATE_TURNING_ON:
                return "正在开启";
            default:
                return "未知状态";
        }
    }
    
    /**
     * 获取蓝牙适配器名称
     */
    public String getBluetoothName() {
        if (!isBluetoothSupported()) {
            return "不支持蓝牙";
        }
        
        // 检查权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(context, 
                    android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return "缺少权限";
            }
        }
        
        try {
            String name = bluetoothAdapter.getName();
            return name != null ? name : "未知设备";
        } catch (SecurityException e) {
            return "权限不足";
        }
    }
    
    /**
     * 获取蓝牙MAC地址（注意：Android 6.0+返回固定值）
     */
    public String getBluetoothAddress() {
        if (!isBluetoothSupported()) {
            return "不支持蓝牙";
        }
        
        try {
            String address = bluetoothAdapter.getAddress();
            return address != null ? address : "未知地址";
        } catch (SecurityException e) {
            return "权限不足";
        }
    }
} 