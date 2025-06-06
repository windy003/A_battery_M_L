package com.example.battery_server;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import java.net.NetworkInterface;
import java.net.InetAddress;
import java.util.Enumeration;

public class BatteryService extends Service {
    
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "BatteryServerChannel";
    private static final int PORT = 8080;
    
    private BatteryHttpServer httpServer;
    private BatteryBroadcastReceiver batteryReceiver;
    
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        registerBatteryReceiver();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;
        
        if ("STOP_SERVICE".equals(action)) {
            stopSelf();
            return START_NOT_STICKY;
        }
        
        startForeground(NOTIFICATION_ID, createNotification());
        startHttpServer();
        
        return START_STICKY; // 服务被杀死后会自动重启
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
        }
        
        if (httpServer != null) {
            httpServer.stop();
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "电池服务器",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("电池服务器后台运行通知");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
    
    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, 
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0
        );
        
        Intent stopIntent = new Intent(this, BatteryService.class);
        stopIntent.setAction("STOP_SERVICE");
        PendingIntent stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0
        );
        
        String ipAddress = getDeviceIpAddress();
        String contentText = ipAddress != null ? 
            "服务器运行中 - " + ipAddress + ":" + PORT : 
            "服务器运行中";
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("电池服务器")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.logo, "停止", stopPendingIntent)
            .setOngoing(true)
            .build();
    }
    
    private void registerBatteryReceiver() {
        batteryReceiver = new BatteryBroadcastReceiver(null);
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);
    }
    
    private void startHttpServer() {
        try {
            if (httpServer == null) {
                httpServer = new BatteryHttpServer(this, PORT);
                httpServer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
} 