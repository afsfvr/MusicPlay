package com.example.musicplayer.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class MusicInfo implements Comparable, Parcelable {

    private int id;
    private String name;
    private String singer;
    private String album;
    /**
     * 总时长，单位毫秒
     */
    private int duration;
    private String path;
    private String parentPath; //父目录路径
    private int love; //1设置我喜欢 0未设置
    private String firstLetter;
    private String lrcPath;


    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getFirstLetter() {
        return firstLetter;
    }

    public void setFirstLetter(String firstLetter) {
        this.firstLetter = firstLetter;
    }

    public int getLove() {
        return love;
    }

    public void setLove(int love) {
        this.love = love;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    public String getLrcPath() {
        return lrcPath;
    }

    public void setLrcPath(String lrcPath) {
        this.lrcPath = lrcPath;
    }

    @Override
    public int compareTo(Object o) {
        MusicInfo info = (MusicInfo) o;
        if (info.getFirstLetter().equals("#"))
            return - 1;
        if (firstLetter.equals("#"))
            return 1;
        return firstLetter.compareTo(info.getFirstLetter());
    }

    @Override
    public String toString() {
        return "MusicInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", singer='" + singer + '\'' +
                ", album='" + album + '\'' +
                ", duration='" + duration + '\'' +
                ", path='" + path + '\'' +
                ", parentPath='" + parentPath + '\'' +
                ", love=" + love +
                ", firstLetter='" + firstLetter + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeString(this.singer);
        dest.writeString(this.album);
        dest.writeInt(this.duration);
        dest.writeString(this.path);
        dest.writeString(this.parentPath);
        dest.writeInt(this.love);
        dest.writeString(this.firstLetter);
    }

    public MusicInfo() {
    }

    protected MusicInfo(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.singer = in.readString();
        this.album = in.readString();
        this.duration = in.readInt();
        this.path = in.readString();
        this.parentPath = in.readString();
        this.love = in.readInt();
        this.firstLetter = in.readString();
    }

    public static final Parcelable.Creator<MusicInfo> CREATOR = new Parcelable.Creator<MusicInfo>() {
        @Override
        public MusicInfo createFromParcel(Parcel source) {
            return new MusicInfo(source);
        }

        @Override
        public MusicInfo[] newArray(int size) {
            return new MusicInfo[size];
        }
    };
}

