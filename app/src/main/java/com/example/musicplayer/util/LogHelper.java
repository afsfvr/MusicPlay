package com.example.musicplayer.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class LogHelper extends Thread {
    private static LogHelper instance;
    private final File outFile;
    private final File pathFile;
    private boolean run;
    private int pid;
    /**
     *
     * 日志等级：*:v , *:d , *:w , *:e , *:f , *:s
     * 显示当前mPID程序的 E和W等级的日志.
     *
     * */

    // cmds = "logcat *:e *:w | grep \"(" + mPID + ")\"";
    // cmds = "logcat  | grep \"(" + mPID + ")\"";//打印所有日志信息
    //cmds = "logcat";//打印所有日志信息
    // cmds = "logcat -s way";//打印标签过滤信息
    //cmds = "logcat *:e *:i | grep \"(" + mPID + ")\""
    private final String cmd;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private LogHelper(Context context) {
        super("日志输出");
        String date = LocalDate.now().format(DATE_FORMAT);
        pathFile = new File(context.getExternalCacheDir().getParent(), "logs");
        if (! pathFile.exists()) pathFile.mkdirs();
        outFile = new File(pathFile, date + ".log");
        if (! outFile.exists()) {
            try {
                outFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        pid = android.os.Process.myPid();
        cmd = "logcat *:e *:w | grep \"(" + pid + ")\"";
        run = true;
        deleteLog();
    }

    public int getPid() {
        return pid;
    }

    private void deleteLog() {
        File[] files = pathFile.listFiles();
        if (files != null && files.length > Constant.MAX_LOG_NUM) {
            ArrayList<String> list = new ArrayList<>();
            for (File file : files) {
                String path = file.getAbsolutePath();
                list.add(path.substring(0, path.lastIndexOf('.')));
            }
            list.sort(String::compareTo);
            for (int i = 0; i < list.size() - Constant.MAX_LOG_NUM; i++) {
                new File(list.get(i)).delete();
            }
        }
    }

    public static LogHelper getInstance(Context context) {
        if (instance == null) {
            instance = new LogHelper(context);
        }
        return instance;
    }

    public void stopLog() {
        run = false;
    }

    @Override
    public void run() {
        super.run();
        InputStream input = null;
        Process process = null;
        try (FileOutputStream output = new FileOutputStream(outFile, true)) {
            process = Runtime.getRuntime().exec(cmd);
            input = process.getInputStream();
            byte[] bytes = new byte[1024];
            int read;
            while (run && (read = input.read(bytes)) != - 1) {
                output.write(bytes, 0, read);
            }
        } catch (Exception e) {
            Log.e("LogHelper", Log.getStackTraceString(e));
        } finally {
            try {
                if (input != null) input.close();
            } catch (IOException ignored) {
            }
            if (process != null) process.destroy();
        }
    }
}