package com.newe.rangrang.permission;

import android.app.Activity;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by Jaylen Hsieh on 2018/04/27.
 */
public class PermissionManager {


    /**
     *
     * @param context
     * @param perms
     * @return true: 已获得权限，false: 未获得权限，主动请求权限
     */
    public static boolean checkPermission(Activity context, String[] perms){
        return EasyPermissions.hasPermissions(context,perms);
    }

    public static void requestPermission(Activity context,String tip, int requestCode, String[] perms){
        EasyPermissions.requestPermissions(context,tip,requestCode,perms);
    }
}
