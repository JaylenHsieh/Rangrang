package com.newe.rangrang.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

import java.io.InputStream;


/**
 * 图片处理工具类
 *
 * @author Jaylen Hsieh
 * @date 2018/04/27.
 */
public class BitmapUtils {

    /**
     * 加了互斥锁的图片文件转码方法
     *
     * @param context 上下文
     * @param path 图片路径
     * @param targetWidth
     * @param targetHeight
     * @return
     */
    public synchronized static Bitmap decodeFileBitmap(Context context, String path, int targetWidth, int targetHeight) {
        try {
            //进行设置
            BitmapFactory.Options options = new BitmapFactory.Options();
            //只获取大小，为快速将布局调整好
            options.inJustDecodeBounds = true;
            //解码
            decodeStreamToBitmap(context, path, options);
            //
            options.inSampleSize = calculateScaleSize(options, targetWidth, targetHeight);
            options.inJustDecodeBounds = false;
            Bitmap bitmap = decodeStreamToBitmap(context, path, options);
            return getNormalBitmap(bitmap, path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Bitmap decodeStreamToBitmap(Context context, String path, BitmapFactory.Options options) {
        Bitmap bitmap = null;
        ContentResolver contentResolver = context.getContentResolver();
        try {
            //MIME type需要添加前缀
            InputStream inputStream = contentResolver.openInputStream(Uri.parse(path.contains("file:") ? path : "file://" + path));
            bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 采用向上取整的方式，计算压缩尺寸
     *
     * @param options
     * @param targetWith
     * @param targetHeight
     * @return
     */
    private static int calculateScaleSize(BitmapFactory.Options options, int targetWith, int targetHeight) {
        int simpleSize;
        if (targetWith > 0 && targetHeight > 0) {
            int scaleWith = (int) Math.ceil((options.outWidth * 1.0f) / targetWith);
            int scaleHeight = (int) Math.ceil((options.outHeight * 1.0f) / targetHeight);
            simpleSize = Math.max(scaleWith, scaleHeight);
        } else {
            simpleSize = 1;
        }
        if (simpleSize == 0) {
            simpleSize = 1;
        }
        return simpleSize;
    }

    /**
     * 根据存储的bitmap中旋转角度，来创建正常的bitmap
     *
     * @param bitmap
     * @param path
     * @return
     */
    private static Bitmap getNormalBitmap(Bitmap bitmap, String path) {
        int rotate = getBitmapRotate(path);
        Bitmap normalBitmap;
        switch (rotate) {
            case 90:
            case 180:
            case 270:
                try {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(rotate);
                    normalBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    if (bitmap != null && !bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    normalBitmap = bitmap;
                }
                break;
            default:
                normalBitmap = bitmap;
                break;
        }
        return normalBitmap;
    }

    /**
     * ExifInterface ：这个类为jpeg文件记录一些image 的标记
     * 这里，获取图片的旋转角度
     *
     * @param path
     * @return
     */
    private static int getBitmapRotate(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return degree;
    }
}
