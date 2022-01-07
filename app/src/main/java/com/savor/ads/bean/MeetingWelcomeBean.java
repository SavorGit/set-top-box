package com.savor.ads.bean;

import java.io.Serializable;
import java.util.List;

public class MeetingWelcomeBean implements Serializable {

    private String id;
    private String forscreen_char;
    private String color;
    private String finish_time;
    private List<ProjectionImg> img_list;
    private int font_id;
    private String fontPath;
    private String font_oss_addr;
    private String music_id;
    private String musicPath;
    private String music_oss_addr;
    private int play_times;
    private String wordsize;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getForscreen_char() {
        return forscreen_char;
    }

    public void setForscreen_char(String forscreen_char) {
        this.forscreen_char = forscreen_char;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getFinish_time() {
        return finish_time;
    }

    public void setFinish_time(String finish_time) {
        this.finish_time = finish_time;
    }

    public List<ProjectionImg> getImg_list() {
        return img_list;
    }

    public void setImg_list(List<ProjectionImg> img_list) {
        this.img_list = img_list;
    }

    public int getFont_id() {
        return font_id;
    }

    public void setFont_id(int font_id) {
        this.font_id = font_id;
    }

    public String getFontPath() {
        return fontPath;
    }

    public void setFontPath(String fontPath) {
        this.fontPath = fontPath;
    }

    public String getFont_oss_addr() {
        return font_oss_addr;
    }

    public void setFont_oss_addr(String font_oss_addr) {
        this.font_oss_addr = font_oss_addr;
    }

    public String getMusic_id() {
        return music_id;
    }

    public void setMusic_id(String music_id) {
        this.music_id = music_id;
    }

    public String getMusicPath() {
        return musicPath;
    }

    public void setMusicPath(String musicPath) {
        this.musicPath = musicPath;
    }

    public String getMusic_oss_addr() {
        return music_oss_addr;
    }

    public void setMusic_oss_addr(String music_oss_addr) {
        this.music_oss_addr = music_oss_addr;
    }

    public int getPlay_times() {
        return play_times;
    }

    public void setPlay_times(int play_times) {
        this.play_times = play_times;
    }

    public String getWordsize() {
        return wordsize;
    }

    public void setWordsize(String wordsize) {
        this.wordsize = wordsize;
    }
}
