package com.example.musicplayer.util;

import com.example.musicplayer.activity.HomeActivity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;

public class ShutdownThread extends Thread {
    private LocalDateTime exitTime;
    private final HomeActivity activity;

    public ShutdownThread(LocalDateTime exitTime, HomeActivity activity) {
        super("定时关闭");
        this.exitTime = exitTime;
        this.activity = activity;
    }

    @Override
    public void run() {
        while (exitTime != null) {
            try {
                if (exitTime.isBefore(LocalDateTime.now())) {
                    exitTime = null;
                    MyApplication.exitApp();
                } else {
                    Duration duration = Duration.between(LocalDateTime.now(), exitTime);
                    long seconds = duration.getSeconds();
                    String title = String.format(Locale.getDefault(),
                            "%02d:%02d:%02d",
                            seconds / 3600, seconds / 60 % 60, seconds % 60);
                    activity.runOnUiThread(() -> activity.getExitItem().setTitle(title));
                }

                Thread.sleep(300);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }
}