package com.newe.rangrang;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


/**
 * 用于使用闪光灯功能的碎片
 * @author Jaylen Hsieh
 * @date 2018/04/22.
 */
public class FlashFragment extends Fragment {

    private CameraManager mCameraManager;
    private Context mContext;
    private FloatingActionButton mFab;
    private boolean isFlashOn = false;
    private boolean isGlitter = false;

    //定时器，用来实现闪光操作
    Timer mTimer;
    TimerTask mTimerTask;

    //代码提示告诉我要用这个代替Timer
    private ScheduledExecutorService mExecutorService;

    private final int PERMISSION_REQUEST_CAMERA = 1;

    public FlashFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_flash, container, false);
        mFab = view.findViewById(R.id.fab_flash);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // 获取 context
        if (mContext == null) {
            mContext = getContext();
        }
        if (mTimer == null){
            mTimer = new Timer();
        }
        if (mTimerTask == null){
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    if (isGlitter) {
                        isGlitter = false;
                        openTorch(false);
                    } else {
                        isGlitter = true;
                        openTorch(true);
                    }
                }
            };
        }

        // 动态权限申请，详细原因参见《第一行代码》7.2，相机为敏感权限
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        } else {
            openTorch(true);
        }

        // 获取相机管理器
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFlashOn) {
                    // 点击后,如果闪光灯是打开的，关闭闪光灯，图标显示为手电关闭，提示用户已关闭闪光灯
                    mFab.setImageDrawable(getResources().getDrawable(R.mipmap.ic_flash_off));
                    Toast.makeText(getContext(), "已关闭闪光灯", Toast.LENGTH_SHORT).show();
                    isFlashOn = false;
                    openTorch(false);
                    mTimer.cancel();
                    mTimerTask.cancel();
                } else {
                    mFab.setImageDrawable(getResources().getDrawable(R.mipmap.ic_flash_on));
                    Toast.makeText(getContext(), "已打开闪光灯", Toast.LENGTH_SHORT).show();
                    isFlashOn = true;
                    openTorch(true);
                    mTimer.schedule(mTimerTask,0,500);
                }

            }
        });


    }

    /**
     * Fragment销毁时关闭闪光灯，避免占用硬件
     */
    @Override
    public void onStop() {
        super.onStop();

        if (mCameraManager != null) {
            try {
                mCameraManager.setTorchMode("0", false);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        mTimerTask.cancel();
        mTimer.cancel();
    }

    /**
     * 控制闪光灯的打开和关闭
     *
     * @param enable true:开 , false:关
     */
    private void openTorch(boolean enable) {
        if (mCameraManager != null) {
            try {
                mCameraManager.setTorchMode("0", enable);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }


    // 权限申请回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openTorch(true);
            } else {
                Toast.makeText(mContext, "权限被拒绝，我们需要你的相机权限来打开闪光灯", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
