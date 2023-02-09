package com.example.musicplayer.entity;

import android.util.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LrcRow implements Comparable<LrcRow> {
    public static final String TAG = LrcRow.class.getName();

    /** 该行歌词要开始播放的时间，格式如下：[02:34.14] */
    private String startTimeString;

    /** 该行歌词要开始播放的时间，由[02:34.14]格式转换为long型，
     * 即将2分34秒14毫秒都转为毫秒后 得到的long型值：startTime=02*60*1000+34*1000+14
     */
    private long startTime;

    /** 该行歌词要结束播放的时间，由[02:34.14]格式转换为long型，
     * 即将2分34秒14毫秒都转为毫秒后 得到的long型值：startTime=02*60*1000+34*1000+14
     */
    private long endTime;

    /** 该行歌词的内容 */
    private String content;


    public LrcRow() {}

    public String getStartTimeString() {
        return startTimeString;
    }

    public void setStartTimeString(String startTimeString) {
        this.startTimeString = startTimeString;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "LrcRow{" +
                "startTimeString='" + startTimeString + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", content='" + content + '\'' +
                '}';
    }

    /**
     * 排序的时候，根据歌词的时间来排序
     */
    public int compareTo(LrcRow another) {
        return (int) (this.startTime - another.startTime);
    }

    public static List<LrcRow> createLrcRows(String path) {
        File file = new File(path);
        ArrayList<LrcRow> rows = new ArrayList<>();
        if (file.exists()) {
            try (FileReader fr = new FileReader(file);
                 BufferedReader reader = new BufferedReader(fr)) {
                String s;
                while ((s = reader.readLine()) != null) {
                    List<LrcRow> row = LrcRow.createRows(s);
                    if (row != null) {
                        rows.addAll(row);
                    }
                }
            } catch (Exception ignored) {
            }
            Collections.sort(rows);
            for (int i = 0; i < rows.size(); i++) {
                if (i < rows.size() - 1) {
                    rows.get(i).setEndTime(rows.get(i + 1).getStartTime());
                } else {
                    rows.get(i).setEndTime(rows.get(i).getStartTime() + 10000);
                }
            }
            rows.removeIf(lrcRow -> lrcRow.getContent().trim().length() == 0);
        }
        return rows;
    }

    /**
     * 处理一行歌词
     * 读取歌词的每一行内容，转换为LrcRow，加入到集合中
     */
    public static List<LrcRow> createRows(String standardLrcLine) {
        /**
         一行歌词只有一个时间的  例如：徐佳莹   《我好想你》
         [01:15.33]我好想你 好想你
         一行歌词有多个时间的  例如：草蜢 《失恋战线联盟》
         [02:34.14][01:07.00]当你我不小心又想起她
         [02:45.69][02:42.20][02:37.69][01:10.60]就在记忆里画一个叉
         **/
        try {
            standardLrcLine = standardLrcLine.trim();
            if (standardLrcLine.indexOf("[") != 0 || (standardLrcLine.indexOf("]") != 9 && standardLrcLine.indexOf("]") != 10)) {
                return null;
            }
            //[02:34.14][01:07.00]当你我不小心又想起她
            //找到最后一个 ‘]’ 的位置
            int lastIndexOfRightBracket = standardLrcLine.lastIndexOf("]");
            //歌词内容就是 ‘]’ 的位置之后的文本   eg:   当你我不小心又想起她
            String content = standardLrcLine.substring(lastIndexOfRightBracket + 1, standardLrcLine.length());
            //歌词时间就是 ‘]’ 的位置之前的文本   eg:   [02:34.14][01:07.00]

            /**
             将时间格式转换一下  [mm:ss.SS][mm:ss.SS] 转换为  -mm:ss.SS--mm:ss.SS-
             即：[02:34.14][01:07.00]  转换为      -02:34.14--01:07.00-
             */
            String times = standardLrcLine.substring(0, lastIndexOfRightBracket + 1).replace("[", "-").replace("]", "-");
            //通过 ‘-’ 来拆分字符串
            String arrTimes[] = times.split("-");
            List<LrcRow> listTimes = new ArrayList<LrcRow>();
            for (int i = 0; i < arrTimes.length; i++) {
                String temp = arrTimes[i];

                if (temp.trim().length() == 0) {
                    continue;
                }

                /** [02:34.14][01:07.00]当你我不小心又想起她
                 *
                 上面的歌词的就可以拆分为下面两句歌词了
                 [02:34.14]当你我不小心又想起她
                 [01:07.00]当你我不小心又想起她
                 */
                LrcRow lrcRow = new LrcRow();
                lrcRow.setContent(content);
                lrcRow.setStartTimeString(temp);
                long startTime = 0;
                try {
                    startTime = timeConvert(temp);
                } catch (Exception e) {
                    Log.i(TAG, Log.getStackTraceString(e));
                }
                lrcRow.setStartTime(startTime);
                listTimes.add(lrcRow);
            }
            return listTimes;
        } catch (Exception e) {
            Log.e(TAG, "createRows exception:" + Log.getStackTraceString(e));
            return null;
        }
    }

    private static long timeConvert(String timeString) {
        //因为给如的字符串的时间格式为XX:XX.XX,返回的long要求是以毫秒为单位
        //将字符串 XX:XX.XX 转换为 XX:XX:XX
        timeString = timeString.replace('.', ':');
        //将字符串 XX:XX:XX 拆分
        String[] times = timeString.split(":");
        // mm:ss:SS
        return Integer.parseInt(times[0]) * 60 * 1000L +//分
                Integer.parseInt(times[1]) * 1000L +//秒
                Integer.parseInt(times[2]);//毫秒
    }
}