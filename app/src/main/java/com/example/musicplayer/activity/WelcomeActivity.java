package com.example.musicplayer.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.musicplayer.R;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


public class WelcomeActivity extends BaseActivity {

    private static final String TAG = WelcomeActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        initPermission();
    }

    private void startMusicActivity() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);
                startActivity(intent);
                WelcomeActivity.this.finish();
            }
        }, 500);
    }

    private void initPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "有权限，无需申请");
            startMusicActivity();
        } else {
            ActivityCompat.requestPermissions(WelcomeActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Log.d(TAG, "申请权限失败！");
                Toast.makeText(this, "必须同意读写文件权限才能使用本程序", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            Log.d(TAG, "申请权限成功！");
            startMusicActivity();
        }
    }

}
