package com.example.battery_server;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import fi.iki.elonen.NanoHTTPD;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BatteryHttpServer extends NanoHTTPD {
    
    private final Context context;
    
    public BatteryHttpServer(Context context, int port) throws IOException {
        super(port);
        this.context = context;
    }
    
    @Override
    public Response serve(IHTTPSession session) {
        if (session == null) {
            return createErrorResponse("无效的会话");
        }
        
        String uri = session.getUri();
        if (uri == null) {
            uri = "/";
        }
        
        Response response;
        
        if ("/battery".equals(uri)) {
            response = getBatteryInfoResponse();
        } else {
            response = newFixedLengthResponse("电池服务器运行中。请访问 /battery 获取电池信息");
        }
        
        // 添加CORS头部
        if (response != null) {
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            response.addHeader("Access-Control-Allow-Headers", "Content-Type");
        }
        
        return response;
    }
    
    private Response getBatteryInfoResponse() {
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, filter);
            
            if (batteryStatus != null) {
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                int temperature = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
                int voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
                int plugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                
                if (scale > 0) {
                    float batteryPct = level * 100.0f / scale;
                    
                    boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                                       status == BatteryManager.BATTERY_STATUS_FULL;
                    
                    String chargingStatus = getChargingStatusText(status);
                    String powerSource = getPowerSourceText(plugged);
                    boolean isPowerConnected = plugged != 0;
                    
                    JSONObject batteryInfo = new JSONObject();
                    batteryInfo.put("level", Math.round(batteryPct * 10) / 10.0);
                    batteryInfo.put("scale", scale);
                    batteryInfo.put("status", chargingStatus);
                    batteryInfo.put("temperature", Math.round(temperature / 10.0 * 10) / 10.0);
                    batteryInfo.put("voltage", voltage);
                    batteryInfo.put("isCharging", isCharging);
                    batteryInfo.put("isPowerConnected", isPowerConnected);
                    batteryInfo.put("powerSource", powerSource);
                    
                    // 添加详细的充电状态说明
                    if (batteryPct >= 100.0f && isPowerConnected) {
                        batteryInfo.put("chargingDetail", 
                            status == BatteryManager.BATTERY_STATUS_CHARGING ? "正在充电" : "维持电量");
                    } else if (isPowerConnected) {
                        batteryInfo.put("chargingDetail", 
                            status == BatteryManager.BATTERY_STATUS_CHARGING ? "正在充电" : "已停止充电");
                    } else {
                        batteryInfo.put("chargingDetail", "未连接电源");
                    }
                    
                    return newFixedLengthResponse(Response.Status.OK, "application/json", batteryInfo.toString());
                }
            }
            
            return createErrorResponse("无法获取电池信息");
            
        } catch (Exception e) {
            JSONObject error = new JSONObject();
            try {
                error.put("error", "获取电池信息时发生错误");
                error.put("message", e.getMessage());
            } catch (Exception jsonException) {
                // 忽略JSON异常
            }
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", error.toString());
        }
    }
    
    private Response createErrorResponse(String errorMessage) {
        try {
            JSONObject error = new JSONObject();
            error.put("success", false);
            error.put("error", errorMessage != null ? errorMessage : "未知错误");
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", error.toString());
        } catch (Exception ex) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "服务器内部错误");
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
    
    private String getPowerSourceText(int plugged) {
        switch (plugged) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                return "交流电源";
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
} 