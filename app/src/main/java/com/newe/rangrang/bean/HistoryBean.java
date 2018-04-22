package com.newe.rangrang.bean;

/**
 * Created by Jaylen Hsieh on 2018/04/22.
 */
public class HistoryBean {

    private String time;
    private String location;
    private int photo;

    public HistoryBean(String time, String location, int photo) {
        this.time = time;
        this.location = location;
        this.photo = photo;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getPhoto() {
        return photo;
    }

    public void setPhoto(int photo) {
        this.photo = photo;
    }
}
