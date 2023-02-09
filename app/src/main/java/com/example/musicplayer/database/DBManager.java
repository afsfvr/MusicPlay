package com.example.musicplayer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.example.musicplayer.entity.MusicInfo;
import com.example.musicplayer.entity.PlayListInfo;
import com.example.musicplayer.util.ChineseToEnglish;
import com.example.musicplayer.util.Constant;
import com.example.musicplayer.util.MyMusicUtil;

import java.util.ArrayList;
import java.util.List;

import static com.example.musicplayer.database.DatabaseHelper.ID_COLUMN;
import static com.example.musicplayer.database.DatabaseHelper.MUSIC_ID_COLUMN;

public class DBManager {

    private static final String TAG = DBManager.class.getName();
    private final DatabaseHelper helper;
    private final SQLiteDatabase db;
    private static DBManager instance = null;

    public DBManager(Context context) {
        helper = new DatabaseHelper(context);
        db = helper.getWritableDatabase();
    }

    public static synchronized DBManager getInstance(Context context) {
        if (instance == null) {
            instance = new DBManager(context);
        }
        return instance;
    }

    // 获取音乐表歌曲数量
    public int getMusicCount(int table) {
        int musicCount = 0;
        Cursor cursor = null;
        try {
            switch (table) {
                case Constant.LIST_ALLMUSIC:
                    cursor = db.query(DatabaseHelper.MUSIC_TABLE, null, null, null, null, null, null);
                    break;
                case Constant.LIST_LASTPLAY:
                    cursor = db.query(DatabaseHelper.LAST_PLAY_TABLE, null, null, null, null, null, null);
                    break;
                case Constant.LIST_MYLOVE:
                    cursor = db.query(DatabaseHelper.MUSIC_TABLE, null, DatabaseHelper.LOVE_COLUMN + " = ? ", new String[]{"" + 1}, null, null, null);
                    break;
                case Constant.LIST_MYPLAY:
                    cursor = db.query(DatabaseHelper.PLAY_LIST_TABLE, null, null, null, null, null, null);
                    break;
            }
            if (cursor.moveToFirst()) {
                musicCount = cursor.getCount();
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return musicCount;
    }

    public List<MusicInfo> getAllMusicFromMusicTable() {
        Log.d(TAG, "getAllMusicFromMusicTable: ");
        List<MusicInfo> musicInfoList = new ArrayList<>();
        db.beginTransaction();
        try (Cursor cursor = db.query(DatabaseHelper.MUSIC_TABLE, null, null, null, null, null, null)) {
            musicInfoList = cursorToMusicList(cursor, musicInfoList);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            db.endTransaction();
        }
        return musicInfoList;
    }

    public MusicInfo getSingleMusicFromMusicTable(int id) {
        Log.i(TAG, "getSingleMusicFromMusicTable: ");
        List<MusicInfo> musicInfoList = new ArrayList<>();
        MusicInfo musicInfo = null;
        db.beginTransaction();
        try (Cursor cursor = db.query(DatabaseHelper.MUSIC_TABLE, null, ID_COLUMN + " = ?", new String[]{"" + id}, null, null, null)) {
            musicInfoList = cursorToMusicList(cursor, musicInfoList);
            musicInfo = musicInfoList.get(0);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            db.endTransaction();
        }
        return musicInfo;
    }


    public List<MusicInfo> getAllMusicFromTable(int playList) {
        Log.d(TAG, "getAllMusicFromTable: ");
        List<Integer> idList = getMusicList(playList);
        List<MusicInfo> musicList = new ArrayList<>();
        for (int id : idList) {
            musicList.add(getSingleMusicFromMusicTable(id));
        }
        return musicList;
    }

    public List<PlayListInfo> getMyPlayList() {
        List<PlayListInfo> playListInfos = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.PLAY_LIST_TABLE, null, null, null, null, null, null);
        Cursor cursorCount = null;
        while (cursor.moveToNext()) {
            PlayListInfo playListInfo = new PlayListInfo();
            int id = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(ID_COLUMN)));
            playListInfo.setId(id);
            playListInfo.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.NAME_COLUMN)));
            cursorCount = db.query(DatabaseHelper.PLAY_LISY_MUSIC_TABLE, null, ID_COLUMN + " = ?", new String[]{"" + id}, null, null, null);
            playListInfo.setCount(cursorCount.getCount());
            playListInfos.add(playListInfo);
        }
        if (cursor != null) {
            cursor.close();
        }
        if (cursorCount != null) {
            cursorCount.close();
        }
        return playListInfos;
    }


    public void createPlaylist(String name) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.NAME_COLUMN, name);
        db.insert(DatabaseHelper.PLAY_LIST_TABLE, null, values);
    }

    public List<MusicInfo> getMusicListBySinger(String singer) {
        List<MusicInfo> musicInfoList = new ArrayList<>();
        Cursor cursor = null;
        db.beginTransaction();
        try {
            if (singer == null) {
                String sql = "select * from " + DatabaseHelper.MUSIC_TABLE + " where " + DatabaseHelper.SINGER_COLUMN + " is null";
                cursor = db.rawQuery(sql, null);
            } else {
                String sql = "select * from " + DatabaseHelper.MUSIC_TABLE + " where " + DatabaseHelper.SINGER_COLUMN + " = ? ";
                cursor = db.rawQuery(sql, new String[]{singer});
            }
            musicInfoList = cursorToMusicList(cursor, musicInfoList);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            db.endTransaction();
            if (cursor != null)
                cursor.close();
        }
        return musicInfoList;
    }

    public List<MusicInfo> getMusicListByAlbum(String album) {
        List<MusicInfo> musicInfoList = new ArrayList<>();
        Cursor cursor = null;
        db.beginTransaction();
        try {
            if (album == null) {
                String sql = "select * from " + DatabaseHelper.MUSIC_TABLE + " where " + DatabaseHelper.ALBUM_COLUMN + " is null";
                cursor = db.rawQuery(sql, null);
            } else {
                String sql = "select * from " + DatabaseHelper.MUSIC_TABLE + " where " + DatabaseHelper.ALBUM_COLUMN + " = ? ";
                cursor = db.rawQuery(sql, new String[]{album});
            }
            musicInfoList = cursorToMusicList(cursor, musicInfoList);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            db.endTransaction();
            if (cursor != null)
                cursor.close();
        }
        return musicInfoList;
    }

    public List<MusicInfo> getMusicListByFolder(String folder) {
        List<MusicInfo> musicInfoList = new ArrayList<>();
        Cursor cursor = null;
        db.beginTransaction();
        try {
            String sql = "select * from " + DatabaseHelper.MUSIC_TABLE + " where " + DatabaseHelper.PARENT_PATH_COLUMN + " = ? ";
            cursor = db.rawQuery(sql, new String[]{folder});
            musicInfoList = cursorToMusicList(cursor, musicInfoList);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            db.endTransaction();
            if (cursor != null)
                cursor.close();
        }
        return musicInfoList;
    }

    public ArrayList<Integer> getMusicIdListByPlaylist(int playlistId) {
        Cursor cursor = null;
        db.beginTransaction();
        ArrayList<Integer> list = new ArrayList<>();
        try {
            String sql = "select * from " + DatabaseHelper.PLAY_LISY_MUSIC_TABLE + " where " + ID_COLUMN + " = ? ";
            cursor = db.rawQuery(sql, new String[]{"" + playlistId});
            while (cursor.moveToNext()) {
                int musicId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.MUSIC_ID_COLUMN));
                list.add(musicId);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            db.endTransaction();
            if (cursor != null)
                cursor.close();
        }
        return list;
    }

    public List<MusicInfo> getMusicListByPlaylist(int playlistId) {
        List<MusicInfo> musicInfoList = new ArrayList<>();
        Cursor cursor = null;
        int id;
        db.beginTransaction();
        try {
            String sql = "select * from " + DatabaseHelper.PLAY_LISY_MUSIC_TABLE + " where " + ID_COLUMN + " = ? ORDER BY " + DatabaseHelper.ID_COLUMN;
            cursor = db.rawQuery(sql, new String[]{"" + playlistId});
            while (cursor.moveToNext()) {
                id = cursor.getInt(cursor.getColumnIndexOrThrow(MUSIC_ID_COLUMN));
                musicInfoList.add(getSingleMusicFromMusicTable(id));
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            db.endTransaction();
            if (cursor != null)
                cursor.close();
        }
        return musicInfoList;
    }

    public int insertMusicListToMusicTable(List<MusicInfo> musicInfoList) {
        Log.d(TAG, "insertMusicListToMusicTable: ");
        int count = 0;
        for (MusicInfo musicInfo : musicInfoList) {
            if (insertMusicInfoToMusicTable(musicInfo) != - 1) count++;
        }
        return count;
    }

    //添加歌曲到音乐表
    public long insertMusicInfoToMusicTable(MusicInfo musicInfo) {
        int id = 1;
        try (Cursor cursor = db.rawQuery("select max(id) from " + DatabaseHelper.MUSIC_TABLE, null)) {
            ContentValues values = musicInfoToContentValues(musicInfo);
            if (cursor.moveToFirst()) {
                //设置新添加的ID为最大ID+1
                id = cursor.getInt(0) + 1;
            }
            values.put(ID_COLUMN, id);
            return db.insert(DatabaseHelper.MUSIC_TABLE, null, values);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            return - 1;
        }
    }

    //添加音乐到歌单
    public void addToPlaylist(int playlistId, int musicId) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.ID_COLUMN, playlistId);
        values.put(DatabaseHelper.MUSIC_ID_COLUMN, musicId);
        db.insert(DatabaseHelper.PLAY_LISY_MUSIC_TABLE, null, values);
    }

    //检索音乐是否已经存在歌单中
    public boolean isExistPlaylist(int playlistId, int musicId) {
        boolean result = false;
        Cursor cursor = db.query(DatabaseHelper.PLAY_LISY_MUSIC_TABLE, null, ID_COLUMN + " = ? and " + MUSIC_ID_COLUMN + " = ? ",
                new String[]{"" + playlistId, "" + musicId}, null, null, null);
        if (cursor.moveToFirst()) {
            result = true;
        }
        if (cursor != null) {
            cursor.close();
        }
        return result;
    }

    public int insert(List<MusicInfo> musicInfoList) {
        db.beginTransaction();
        int count = 0;
        try {
            count = insertMusicListToMusicTable(musicInfoList);
            db.setTransactionSuccessful();
            return count;
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            return count;
        } finally {
            db.endTransaction();
        }
    }

    public void updateAllMusic(List<MusicInfo> musicInfoList) {
        db.beginTransaction();
        try {
            deleteAllTable();
            insertMusicListToMusicTable(musicInfoList);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            db.endTransaction();
        }
    }

    public void updateLrcPath(String path, int musicId) {
        if (path == null || path.length() == 0) return;
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.LRC_PATH_COLUMN, path);
            db.update(DatabaseHelper.MUSIC_TABLE, values, ID_COLUMN + " = ? ", new String[]{"" + musicId});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            db.endTransaction();
        }
    }

    //删除数据库中所有的表
    public void deleteAllTable() {
        db.execSQL("PRAGMA foreign_keys=ON");
        db.delete(DatabaseHelper.MUSIC_TABLE, null, null);
        db.delete(DatabaseHelper.LAST_PLAY_TABLE, null, null);
        db.delete(DatabaseHelper.PLAY_LIST_TABLE, null, null);
        db.delete(DatabaseHelper.PLAY_LISY_MUSIC_TABLE, null, null);
    }

    //删除指定音乐
    public void deleteMusic(int id) {
        db.execSQL("PRAGMA foreign_keys=ON");
        db.delete(DatabaseHelper.MUSIC_TABLE, ID_COLUMN + " = ? ", new String[]{"" + id});
        db.delete(DatabaseHelper.LAST_PLAY_TABLE, ID_COLUMN + " = ? ", new String[]{"" + id});
    }

    public void deletePlaylist(int id) {
        db.delete(DatabaseHelper.PLAY_LIST_TABLE, ID_COLUMN + " = ? ", new String[]{"" + id});
    }

    //根据从哪个activity中发出的移除歌曲指令判断
    public void removeMusic(int id, int witchActivity) {
        db.execSQL("PRAGMA foreign_keys=ON");
        switch (witchActivity) {
            case Constant.ACTIVITY_LOCAL:
                db.delete(DatabaseHelper.MUSIC_TABLE, ID_COLUMN + " = ? ", new String[]{"" + id});
                break;
            case Constant.ACTIVITY_RECENTPLAY:
                db.delete(DatabaseHelper.LAST_PLAY_TABLE, ID_COLUMN + " = ? ", new String[]{"" + id});
                break;
            case Constant.ACTIVITY_MYLOVE:
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.LOVE_COLUMN, 0);
                db.update(DatabaseHelper.MUSIC_TABLE, values, ID_COLUMN + " = ? ", new String[]{"" + id});
                break;
        }
    }

    //根据从哪个activity中发出的移除歌曲指令判断
    public int removeMusicFromPlaylist(int musicId, int playlistId) {
        db.execSQL("PRAGMA foreign_keys=ON");
        int ret = 0;
        try {
            ret = db.delete(DatabaseHelper.PLAY_LISY_MUSIC_TABLE, ID_COLUMN + " = ? and " + DatabaseHelper.MUSIC_ID_COLUMN
                    + " = ? ", new String[]{"" + playlistId, musicId + ""});
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return ret;
    }

    // 获取歌曲路径
    public String getMusicPath(int id) {
        Log.d(TAG, "getMusicPath id = " + id);
        if (id == - 1) {
            return null;
        }
        String path = null;
        Cursor cursor = null;
        setLastPlay(id);        //每次播放一首新歌前都需要获取歌曲路径，所以可以在此设置最近播放
        try {
            cursor = db.query(DatabaseHelper.MUSIC_TABLE, null, ID_COLUMN + " = ?", new String[]{"" + id}, null, null, null);
            Log.i(TAG, "getCount: " + cursor.getCount());
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.PATH_COLUMN));
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }

    //获取音乐表中的第一首音乐的ID
    public int getFirstId() {
        int id = - 1;
        try (Cursor cursor = db.rawQuery("select min(id) from " + DatabaseHelper.MUSIC_TABLE, null)) {
            if (cursor.moveToFirst()) {
                id = Integer.parseInt(cursor.getString(0));
                Log.d(TAG, "getFirstId min id = " + id);
            }
        } catch (Exception ignored) {
        }
        return id;
    }


    // 获取下一首歌曲(id)
    public int getNextMusic(ArrayList<Integer> musicList, int id, int playMode) {
        if (id == - 1) {
            return - 1;
        }
        //找到当前id在列表的第几个位置（i+1）
        int index = musicList.indexOf(id);
        if (index == - 1) {
            return - 1;
        }
        index++;
        // 如果当前是最后一首
        switch (playMode) {
            case Constant.PLAYMODE_SEQUENCE:
                if (index >= musicList.size()) {
                    id = musicList.get(0);
                } else {
                    id = musicList.get(index);
                }
                break;
            case Constant.PLAYMODE_SINGLE_REPEAT:
                break;
            case Constant.PLAYMODE_RANDOM:
                id = getRandomMusic(musicList, id);
                break;
        }
        return id;
    }

    // 获取上一首歌曲(id)
    public int getPreMusic(ArrayList<Integer> musicList, int id, int playMode) {
        if (id == - 1) {
            return - 1;
        }
        //找到当前id在列表的第几个位置（i+1）
        int index = musicList.indexOf(id);
        if (index == - 1) {
            return - 1;
        }
        // 如果当前是第一首则返回最后一首
        switch (playMode) {
            case Constant.PLAYMODE_SEQUENCE:
                if (index == 0) {
                    id = musicList.get(musicList.size() - 1);
                } else {
                    -- index;
                    id = musicList.get(index);
                }
                break;
            case Constant.PLAYMODE_SINGLE_REPEAT:
                break;
            case Constant.PLAYMODE_RANDOM:
                id = getRandomMusic(musicList, id);
                break;
        }
        return id;
    }

    // 获取歌单列表
    public ArrayList<Integer> getMusicList(int playList) {
        Cursor cursor = null;
        ArrayList<Integer> list = new ArrayList<>();
        int musicId;
        switch (playList) {
            case Constant.LIST_ALLMUSIC:
                cursor = db.query(DatabaseHelper.MUSIC_TABLE, null, null, null, null, null, null);
                break;
            case Constant.LIST_LASTPLAY:
                cursor = db.rawQuery("select * from " + DatabaseHelper.LAST_PLAY_TABLE + " ORDER BY " + DatabaseHelper.DURATION_COLUMN + " DESC", null);
                break;
            case Constant.LIST_MYLOVE:
                cursor = db.query(DatabaseHelper.MUSIC_TABLE, null, DatabaseHelper.LOVE_COLUMN + " = ?", new String[]{"" + 1}, null, null, null);
                break;
            case Constant.LIST_PLAYLIST:
                int listId = MyMusicUtil.getIntShared(Constant.KEY_LIST_ID);
                list = getMusicIdListByPlaylist(listId);
                break;
            default:
                Log.e(TAG, "getMusicList default");
                break;
        }
        if (cursor != null) {
            while (cursor.moveToNext()) {
                musicId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                list.add(musicId);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return list;
    }

    // 获取歌曲详细信息
    public ArrayList<String> getMusicInfo(int id) {
        Cursor cursor = null;
        ArrayList<String> musicInfo = new ArrayList<>();
        cursor = db.query(DatabaseHelper.MUSIC_TABLE, null, ID_COLUMN + " = ?", new String[]{"" + id}, null, null, null);
        if (cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                musicInfo.add(i, cursor.getString(i));
            }
        } else {
            musicInfo.add("-1"); // 0 id
            musicInfo.add("听听音乐"); // 1 name
            musicInfo.add("好音质"); // 2 singer
            musicInfo.add("0"); // 3 album
            musicInfo.add("0"); // 4 duration
            musicInfo.add(""); // 5 path
            musicInfo.add(""); // 6 parent path
            musicInfo.add("0"); // 7 love
            musicInfo.add("0"); // 8 first letter
            musicInfo.add(""); // 9 lrc path
        }
        if (cursor != null) {
            cursor.close();
        }
        return musicInfo;
    }

    //获取随机歌曲
    public int getRandomMusic(ArrayList<Integer> list, int id) {
        int musicId;
        if (id == - 1 || list.isEmpty()) {
            return - 1;
        }
        if (list.size() == 1) {
            return id;
        }
        do {
            int count = (int) (Math.random() * list.size());
            musicId = list.get(count);
        } while (musicId == id);

        return musicId;

    }

    //设置最近播放
    public void setLastPlay(int id) {
        Log.i(TAG, "setLastPlay: id = " + id);
        if (id == - 1 || id == 0) {
            return;
        }
        db.beginTransaction();
        try (Cursor cursor = db.rawQuery("select id, count(1) "
                + "from " + DatabaseHelper.LAST_PLAY_TABLE
                + " order by " + DatabaseHelper.DURATION_COLUMN + " desc limit 1", null)) {
            cursor.moveToFirst();
            int count = cursor.getInt(1) - DatabaseHelper.LAST_TABLE_MAX_COUNT;
            if (id != cursor.getInt(0)) {
                count++;
                ContentValues values = new ContentValues();
                values.put(ID_COLUMN, id);
                values.put(DatabaseHelper.DURATION_COLUMN, System.currentTimeMillis());
                db.replace(DatabaseHelper.LAST_PLAY_TABLE, null, values);
            }
            if (count > 0) {
                db.execSQL("delete from " + DatabaseHelper.LAST_PLAY_TABLE
                        + " where " + DatabaseHelper.DURATION_COLUMN + " in (select " + DatabaseHelper.DURATION_COLUMN
                        + " from " + DatabaseHelper.LAST_PLAY_TABLE
                        + " order by " + DatabaseHelper.DURATION_COLUMN + " asc limit " + count + ")");
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            db.endTransaction();
        }
    }

    public void setMyLove(int id) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.LOVE_COLUMN, 1);
        db.update(DatabaseHelper.MUSIC_TABLE, values, ID_COLUMN + " = ? ", new String[]{"" + id});
    }

    //把MusicInfo对象转为ContentValues对象
    public ContentValues musicInfoToContentValues(MusicInfo musicInfo) {
        ContentValues values = new ContentValues();
        try {
            //            values.put(DatabaseHelper.ID_COLUMN, musicInfo.getId());
            values.put(DatabaseHelper.NAME_COLUMN, musicInfo.getName());
            values.put(DatabaseHelper.SINGER_COLUMN, musicInfo.getSinger());
            values.put(DatabaseHelper.ALBUM_COLUMN, musicInfo.getAlbum());
            values.put(DatabaseHelper.DURATION_COLUMN, musicInfo.getDuration());
            values.put(DatabaseHelper.PATH_COLUMN, musicInfo.getPath());
            values.put(DatabaseHelper.PARENT_PATH_COLUMN, musicInfo.getParentPath());
            values.put(DatabaseHelper.LOVE_COLUMN, musicInfo.getLove());
            values.put(DatabaseHelper.FIRST_LETTER_COLUMN, "" + ChineseToEnglish.StringToPinyinSpecial(musicInfo.getName()).toUpperCase().charAt(0));
            values.put(DatabaseHelper.LRC_PATH_COLUMN, musicInfo.getLrcPath());
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return values;
    }

    //把Cursor对象转为List<MusicInfo>对象
    public List<MusicInfo> cursorToMusicList(Cursor cursor, List<MusicInfo> list) {
        try {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(ID_COLUMN));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.NAME_COLUMN));
                    String singer = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.SINGER_COLUMN));
                    String album = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ALBUM_COLUMN));
                    int duration = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.DURATION_COLUMN));
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.PATH_COLUMN));
                    String parentPath = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.PARENT_PATH_COLUMN));
                    int love = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.LOVE_COLUMN));
                    String firstLetter = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.FIRST_LETTER_COLUMN));
                    String lrcPath = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.LRC_PATH_COLUMN));

                    MusicInfo musicInfo = new MusicInfo();
                    musicInfo.setId(id);
                    musicInfo.setName(name);
                    musicInfo.setSinger(singer);
                    musicInfo.setAlbum(album);
                    musicInfo.setPath(path);
                    musicInfo.setParentPath(parentPath);
                    musicInfo.setLove(love);
                    musicInfo.setDuration(duration);
                    musicInfo.setFirstLetter(firstLetter);
                    musicInfo.setLrcPath(lrcPath);
                    list.add(musicInfo);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return list;
    }

}