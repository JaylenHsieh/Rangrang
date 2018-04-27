package com.newe.rangrang.permission;

import android.app.Activity;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * 运行时权限申请的工具类
 * @author Jaylen Hsieh
 * @date 2018/04/27.
 */
public class PermissionManager {


    /**
     * 检查是否已获取该权限的静态方法
     *
     * @param context 上下文，由调用该静态方法的类传入
     * @param perms 权限列表，一般每次只放一个权限
     * @return true: 已获得权限，false: 未获得权限，主动请求权限
     */
    public static boolean checkPermission(Activity context, String[] perms){
        return EasyPermissions.hasPermissions(context,perms);
    }

    /**
     * 运行时申请权限的方法，通过{EasyPermissions}类实现
     *
     * @param context
     * @param tip
     * @param requestCode
     * @param perms
     */
    public static void requestPermission(Activity context,String tip, int requestCode, String[] perms){
        EasyPermissions.requestPermissions(context,tip,requestCode,perms);
    }
}
