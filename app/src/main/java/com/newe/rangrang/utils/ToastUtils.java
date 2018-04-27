package com.newe.rangrang.utils;

import android.content.Context;
import android.widget.Toast;



public class ToastUtils {

    public static void showToast(Context context,String content){
        Toast.makeText(context.getApplicationContext(),content,Toast.LENGTH_SHORT).show();
    }
}
