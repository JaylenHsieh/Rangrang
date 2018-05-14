package com.newe.rangrang.db;

import android.Manifest;

/**
 * 常量类，用来存放写入权限的请求code,提示语，和权限码
 * @author Jaylen Hsieh
 * @date 2018/04/26.
 */

public final class Constance {

    //相机
    public final static int CAMERA_PERMISSION_CODE = 1;
    public final static String CAMERA_PERMISSION_TIP = "我们需要手机的相机权限来打开闪光灯和拍摄视频，也需要手机的读写权限来保存和播放您的视频";
    public final static String[] PERMS_CAMERA = {Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    //手机写文件
    public final static int WRITE_PERMISSION_CODE = 2;
    public final static String WRITE_PERMISSION_TIP = "我们需要手机的读写权限来保存您的照片";
    public final static String[] PERMS_WRITE = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};

    //没搞清啥功能的两个code
    public final static int PICTURE_CODE = 10;
    public final static int GALLERY_CODE = 11;
}
