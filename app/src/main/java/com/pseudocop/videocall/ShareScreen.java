package com.pseudocop.videocall;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


import android.app.Service;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import android.content.pm.PackageManager;
import android.os.Build;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;


import java.util.ArrayList;
import java.util.Objects;

import org.json.JSONObject;

public class ShareScreen extends AppCompatActivity {
    public static MediaProjectionManager mMediaProjectionManager;
    Intent service;
    private static final int REQUEST_MEDIA_PROJECTION = 1;
    public static MediaProjection mMediaProjection;
    private void startScreenShare() {
        Intent intent = mMediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(intent, REQUEST_MEDIA_PROJECTION);
    }
    private VirtualDisplay mVirtualDisplay;

    public interface ShareScreenListener {
        void onScreenShared(MediaProjection mediaProjection);
    }


    private void setupVirtualDisplay() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        int density = metrics.densityDpi;

        SurfaceView surfaceView = findViewById(R.id.screen_share_surface_view);
        Surface surface = surfaceView.getHolder().getSurface();

        mVirtualDisplay = mMediaProjection.createVirtualDisplay("ScreenShare",
                width, height, density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surface, null, null);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_screen);
        if (Build.VERSION.SDK_INT <21) {
            Toast.makeText(getApplicationContext(), "Require Permission for Recording", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // 5.0 and above
            // Request screen recording permission, wait for user authorization
            mMediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
            startScreenShare();
//            startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), 1234);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1234 && resultCode == RESULT_OK) {
            setupVirtualDisplay();
            mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
            if (mListener != null) {
                mListener.onScreenShared(mMediaProjection);
            }
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                //Target version higher than or equal to 10.0 needs to use the foreground service, and create a MediaProjection in the onStartCommand method of the foreground service
//                service = new Intent(ShareScreen.this, MainActivity.class);
//                service.putExtra("code", resultCode);
////                service.putExtra("data", data);
////                startForegroundService(service);
//            } else {
//                //Target version is lower than 10.0 to get MediaProjection directly
//                mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
//            }
        }
    }
    private ShareScreenListener mListener;

    public void setShareScreenListener(ShareScreenListener listener) {
        mListener = listener;
    }

}
