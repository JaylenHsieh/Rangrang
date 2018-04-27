package com.newe.rangrang.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * 方便弹Toast然后不会忘记show一下的工具类
 *
 * @author Jaylen Hsieh
 * @date 2018/04/27.
 */

public class ToastUtils {

    public static void showToast(Context context,String content){
        Toast.makeText(context.getApplicationContext(),content,Toast.LENGTH_SHORT).show();
    }
}
