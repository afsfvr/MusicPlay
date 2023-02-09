package com.example.musicplayer.util;


public class Constant {
    public static final int MAX_LOG_NUM = 5;

    //播放状态
    public static final String STATUS = "status";

    public static final int STATUS_STOP = 0; //停止状态
    public static final int STATUS_PLAY = 1; //播放状态
    public static final int STATUS_PAUSE = 2; //暂停状态
    public static final int STATUS_RUN = 3;  //   状态

    public static final String COMMAND = "cmd";

    public static final int COMMAND_INIT = 1; //初始化命令
    public static final int COMMAND_PLAY = 2; //播放命令
    public static final int COMMAND_PAUSE = 3; //暂停命令
    public static final int COMMAND_STOP = 4; //停止命令
    public static final int COMMAND_PROGRESS = 5; //改变进度命令
    public static final int COMMAND_RELEASE = 6; //退出程序时释放
    public static final int COMMAND_PREV = 7; //上一首
    public static final int COMMAND_NEXT = 8; //下一首
    public static final int COMMAND_UPDATE_NOTIFICATION = 9; //更新通知

    //播放模式
    public static final int PLAYMODE_SEQUENCE = - 1;
    public static final int PLAYMODE_SINGLE_REPEAT = 1;
    public static final int PLAYMODE_RANDOM = 2;

    public static final String PLAYMODE_SEQUENCE_TEXT = "顺序播放";
    public static final String PLAYMODE_RANDOM_TEXT = "随机播放";
    public static final String PLAYMODE_SINGLE_REPEAT_TEXT = "单曲循环";


    //Activity label

    public static final String LABEL = "label";
    public static final String LABEL_MYLOVE = "我喜爱";
    public static final String LABEL_LAST = "最近播放";
    public static final String LABEL_LOCAL = "本地音乐";

    public static final int ACTIVITY_LOCAL = 20; //我喜爱
    public static final int ACTIVITY_RECENTPLAY = 21;//最近播放
    public static final int ACTIVITY_MYLOVE = 22; //我喜爱
    public static final int ACTIVITY_MYLIST = 24;//我的歌单

    //handle常量
    public static final int SCAN_ERROR = 0;
    public static final int SCAN_COMPLETE = 1;
    public static final int SCAN_UPDATE = 2;
    public static final int SCAN_NO_MUSIC = 3;

    //SharedPreferences key 常量
    public static final String KEY_ID = "id";
    public static final String KEY_PATH = "path";
    public static final String KEY_MODE = "mode";
    public static final String KEY_LIST = "list";
    public static final String KEY_LIST_ID = "list_id";
    public static final String KEY_CURRENT = "current";
    public static final String KEY_DURATION = "duration";

    //歌曲列表常量
    public static final int LIST_ALLMUSIC = - 1;
    public static final int LIST_MYLOVE = 10000;
    public static final int LIST_LASTPLAY = 10001;
    public static final int LIST_MYPLAY = 10003; //我的歌单列表
    public static final int LIST_PLAYLIST = 10004;    //歌单音乐列表

    public static final int LIST_SINGER = 10005;    //歌手
    public static final int LIST_ALBUM = 10006;        //专辑
    public static final int LIST_FOLDER = 10007;    //文件夹

    public static final String FLOAT_RECEIVER = "com.example.musicplay.floatReceiver";
    public static final String FLOAT_CHANGE = "change_float_state";

    public static final String THEME = "theme";
    public static final String TITLE = "title";

    public static final int NOTIFICATION_ID = 1;
    public static final String CHANNEL_ID = "music_play";
    public static final String CHANNEL_NAME = "music_play";
}
