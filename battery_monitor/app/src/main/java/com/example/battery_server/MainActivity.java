package com.example.battery_server;

import android.content.IntentFilter;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.net.NetworkInterface;
import java.net.InetAddress;
import java.util.Enumeration;
import android.os.Build;

public class MainActivity extends AppCompatActivity {
    
    private static final int PORT = 8080;
    
    private TextView statusText;
    private TextView batteryInfoText;
    private TextView ipAddressText;
    private BatteryBroadcastReceiver batteryReceiver;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        registerBatteryReceiver();
        showIpAddress();
        startBackgroundService();
        updateBatteryInfo();
    }
    
    private void initViews() {
        statusText = findViewById(R.id.statusText);
        batteryInfoText = findViewById(R.id.batteryInfoText);
        ipAddressText = findViewById(R.id.ipAddressText);
        
        statusText.setText("正在启动服务器...");
        batteryInfoText.setText("电池信息加载中...");
        ipAddressText.setText("正在获取IP地址...");
    }
    
    private void registerBatteryReceiver() {
        batteryReceiver = new BatteryBroadcastReceiver(this);
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);
    }
    
    private void showIpAddress() {
        String ipAddress = getDeviceIpAddress();
        if (ipAddress != null) {
            ipAddressText.setText("手机IP地址: " + ipAddress + ":" + PORT);
        } else {
            ipAddressText.setText("无法获取IP地址，请检查WiFi连接");
        }
    }
    
    private String getDeviceIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address.isLoopbackAddress()) {
                        continue;
                    }
                    
                    String hostAddress = address.getHostAddress();
                    if (hostAddress == null) {
                        continue;
                    }
                    
                    // 检查是否为IPv4地址
                    if (!hostAddress.contains(":")) {
                        return hostAddress;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private void startBackgroundService() {
        Intent serviceIntent = new Intent(this, BatteryService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        BatteryTileService.setServiceRunning(true);
        statusText.setText("后台服务已启动 - 端口: " + PORT);
        Toast.makeText(this, "后台服务启动成功！", Toast.LENGTH_SHORT).show();
    }
    
    public void updateBatteryInfo() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, filter);
                
        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            int temperature = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
            int voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
            
            // 获取电源插入状态
            int plugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            
            if (scale > 0) {
                float batteryPct = level * 100.0f / scale;
                
                // 获取详细的充电和电源状态
                String detailedStatus = getDetailedChargingStatus(status, plugged, batteryPct);
                
                String info = String.format(
                    "电量: %.1f%%\n%s\n温度: %.1f°C\n电压: %d mV",
                    batteryPct, 
                    detailedStatus,
                    temperature / 10.0f,
                    voltage
                );
                
                batteryInfoText.setText(info);
            } else {
                batteryInfoText.setText("无法获取电池信息");
            }
        } else {
            batteryInfoText.setText("电池状态不可用");
        }
    }
    
    private String getDetailedChargingStatus(int status, int plugged, float batteryPct) {
        // 首先判断电源是否插入
        String powerSource = getPowerSourceText(plugged);
        String chargingStatus = getChargingStatusText(status);
        
        // 特殊处理100%的情况
        if (batteryPct >= 100.0f) {
            if (plugged != 0) { // 电源已插入
                return String.format("状态: %s\n电源: %s\n充电: %s", 
                    chargingStatus, 
                    powerSource,
                    status == BatteryManager.BATTERY_STATUS_CHARGING ? "正在充电" : "维持电量");
            } else {
                return String.format("状态: %s\n电源: 未插入", chargingStatus);
            }
        } else {
            // 电量未满的情况
            if (plugged != 0) { // 电源已插入
                return String.format("状态: %s\n电源: %s\n充电: %s", 
                    chargingStatus, 
                    powerSource,
                    status == BatteryManager.BATTERY_STATUS_CHARGING ? "正在充电" : "已停止充电");
            } else {
                return String.format("状态: %s\n电源: 未插入", chargingStatus);
            }
        }
    }
    
    private String getPowerSourceText(int plugged) {
        switch (plugged) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                return "交流电源 (AC)";
            case BatteryManager.BATTERY_PLUGGED_USB:
                return "USB充电";
            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                return "无线充电";
            case 0:
                return "未插入";
            default:
                return "未知电源";
        }
    }
    
    private String getChargingStatusText(int status) {
        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                return "充电中";
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                return "放电中";
            case BatteryManager.BATTERY_STATUS_FULL:
                return "已充满";
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                return "未充电";
            case BatteryManager.BATTERY_STATUS_UNKNOWN:
            default:
                return "未知状态";
        }
    }
    
    public void onBatteryChanged() {
        updateBatteryInfo();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
        }
    }
}
