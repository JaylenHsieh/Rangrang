package com.newe.rangrang.bean;

/**
 * 历史记录的实体类，用来存放历史记录的数据：时间，地点， （可缺省的）照片
 * @author  Jaylen Hsieh
 * @date 2018/04/22.
 */
public class HistoryBean {

    private String time;
    private String location;
    //照片在resource中的id号
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
