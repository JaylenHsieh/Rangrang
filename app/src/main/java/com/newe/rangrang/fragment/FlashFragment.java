package com.newe.rangrang.fragment;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.newe.rangrang.R;
import com.newe.rangrang.db.Constance;
import com.newe.rangrang.permission.PermissionManager;
import com.newe.rangrang.utils.ToastUtils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import rx.Observable;
import rx.Single;


/**
 * 闪光灯功能的Fragment
 *
 * @author Jaylen Hsieh
 * @date 2018/04/27.
 */
public class FlashFragment extends Fragment implements EasyPermissions.PermissionCallbacks {

    private CameraManager mCameraManager;
    private Context mContext;
    private FloatingActionButton mFab;
    private boolean isFlashOn = false;
    private boolean isGlittering = false;


    public FlashFragment() {
        // Required empty public constructor
    }

    CountDownTimer mCountDownTimer = new CountDownTimer(Long.MAX_VALUE, 500) {
        @Override
        public void onTick(long l) {
            isGlittering = !isGlittering;
            openTorch(isGlittering);
        }

        @Override
        public void onFinish() {
            isGlittering = false;
            openTorch(isGlittering);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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


        // 获取相机管理器
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkCameraPermission();
                if (isFlashOn) {
                    // 点击后,如果闪光灯是打开的，关闭闪光灯，图标显示为手电关闭，提示用户已关闭闪光灯
                    mFab.setImageDrawable(getResources().getDrawable(R.mipmap.ic_flash_off, getActivity().getTheme()));
                    Toast.makeText(getContext(), "已关闭闪光灯", Toast.LENGTH_SHORT).show();
                    isFlashOn = false;
                    openTorch(false);
                    mCountDownTimer.cancel();
                } else {
                    mFab.setImageDrawable(getResources().getDrawable(R.mipmap.ic_flash_on));
                    Toast.makeText(getContext(), "已打开闪光灯", Toast.LENGTH_SHORT).show();
                    isFlashOn = true;
                    openTorch(true);
                    mCountDownTimer.start();
                }
            }
        });


    }

    /**
     * 检查相机权限
     */
    private void checkCameraPermission() {
        boolean result = PermissionManager.checkPermission(getActivity(), Constance.PERMS_CAMERA);
        if (!result) {
            PermissionManager.requestPermission(getActivity(), Constance.CAMERA_PERMISSION_TIP, Constance.CAMERA_PERMISSION_CODE, Constance.PERMS_CAMERA);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE:
                break;
            default:
                break;
        }
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
    }

    /**
     * 控制闪光灯的打开和关闭
     *
     * @param enable
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


    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        ToastUtils.showToast(mContext, "用户授权成功");
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        ToastUtils.showToast(mContext, "用户授权失败");
        /**
         * 若是在权限弹窗中，用户勾选了'NEVER ASK AGAIN.'或者'不在提示'，且拒绝权限。
         * 这时候，需要跳转到设置界面去，让用户手动开启。
         */
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }
}
