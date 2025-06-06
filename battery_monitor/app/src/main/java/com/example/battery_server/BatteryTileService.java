package com.example.battery_server;

import android.content.Intent;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.os.Build;
import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.N)
public class BatteryTileService extends TileService {
    
    private static boolean isServiceRunning = false;
    
    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTile();
    }
    
    @Override
    public void onClick() {
        super.onClick();
        
        if (isServiceRunning) {
            stopBatteryService();
        } else {
            startBatteryService();
        }
        
        updateTile();
    }
    
    private void startBatteryService() {
        Intent serviceIntent = new Intent(this, BatteryService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        isServiceRunning = true;
    }
    
    private void stopBatteryService() {
        Intent serviceIntent = new Intent(this, BatteryService.class);
        serviceIntent.setAction("STOP_SERVICE");
        startService(serviceIntent);
        isServiceRunning = false;
    }
    
    private void updateTile() {
        Tile tile = getQsTile();
        if (tile != null) {
            if (isServiceRunning) {
                tile.setState(Tile.STATE_ACTIVE);
                tile.setLabel("电池服务器");
                tile.setSubtitle("运行中");
            } else {
                tile.setState(Tile.STATE_INACTIVE);
                tile.setLabel("电池服务器");
                tile.setSubtitle("已停止");
            }
            
            tile.setIcon(Icon.createWithResource(this, R.drawable.logo));
            tile.updateTile();
        }
    }
    
    public static void setServiceRunning(boolean running) {
        isServiceRunning = running;
    }
} 