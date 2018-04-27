package com.newe.rangrang.db;

import android.Manifest;

/**
 * 常量类
 */

public final class Constance {
    /**
     * 写入权限的请求code,提示语，和权限码
     */

    public final static int CAMERA_PERMISSION_CODE = 1;
    public final static int WRITE_PERMISSION_CODE = 2;
    public final static String CAMERA_PERMISSION_TIP = "我们需要手机的相机权限来打开闪光灯和拍摄相片";
    public final static String WRITE_PERMISSION_TIP = "我们需要手机的读写权限来保存您的照片";
    public final static String[] PERMS_CAMERA = {Manifest.permission.CAMERA};
    public final static String[] PERMS_WRITE = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    /**
     * 相机，图库的请求code
     */
    public final static int PICTURE_CODE = 10;
    public final static int GALLERY_CODE = 11;
}
