package com.example.musicplayer.util;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.musicplayer.service.MusicPlayerService;

import java.util.List;

public class MyApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        initNightMode();
        LogHelper.getInstance(this).start();
    }

    protected void initNightMode() {
        boolean isNight = MyMusicUtil.getNightMode(context);
        if (isNight) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static void exitApp() {
        Intent intentBroadcast = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
        intentBroadcast.putExtra(Constant.COMMAND, Constant.COMMAND_RELEASE);
        context.sendBroadcast(intentBroadcast);
        Intent stopIntent = new Intent(context, MusicPlayerService.class);
        context.stopService(stopIntent);
        LogHelper.getInstance(context).stopLog();
        new Thread(() -> {
            ActivityManager manager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.AppTask> tasks = manager.getAppTasks();
            tasks.forEach(ActivityManager.AppTask::finishAndRemoveTask);
            try {
                Thread.sleep(600);
            } catch (InterruptedException ignored) {
            }
            System.exit(0);
        }, "exit app").start();
    }

    public static Context getContext() {
        return context;
    }
}
