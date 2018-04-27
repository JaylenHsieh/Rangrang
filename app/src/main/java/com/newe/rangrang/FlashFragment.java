package com.newe.rangrang;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.newe.rangrang.db.Constance;
import com.newe.rangrang.permission.PermissionManager;
import com.newe.rangrang.utils.ToastUtils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;


/**
 * A simple {@link Fragment} subclass.
 */
public class FlashFragment extends Fragment implements EasyPermissions.PermissionCallbacks {

    private CameraManager mCameraManager;
    private Context mContext;
    private FloatingActionButton mFab;
    private boolean isFlashOn = false;
    private boolean isGlitter = false;

    private String cameraPermission = Manifest.permission.CAMERA;
    private final int PERMISSION_REQUEST_CAMERA = 1;

    Timer mTimer;
    TimerTask mTimerTask;

    public FlashFragment() {
        // Required empty public constructor
    }


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

        if (mTimer == null) {
            mTimer = new Timer();
        }
        if (mTimerTask == null) {
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


        checkCameraPermisson();
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
                    mTimer.schedule(mTimerTask, 0, 500);
                }

            }
        });


    }

    // 检查相机权限
    private void checkCameraPermisson() {
        boolean result = PermissionManager.checkPermission(getActivity(), Constance.PERMS_CAMERA);
        if (!result) {
            PermissionManager.requestPermission(getActivity(), Constance.CAMERA_PERMISSION_TIP, Constance.CAMERA_PERMISSION_CODE, Constance.PERMS_CAMERA);
        }
    }

    /**
     * 重写 OnRequestPermissionResult，用于接受请求结果
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 将请求结果传递 EasyPermission库处理
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * 请求权限成功
     *
     * @param requestCode
     * @param perms
     */
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        ToastUtils.showToast(mContext, "授权成功");
    }

    /**
     * 请求权限失败
     *
     * @param requestCode
     * @param perms
     */
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        ToastUtils.showToast(mContext, "权限请求被拒绝");
        /**
         * 若是在权限弹窗中，用户勾选了'NEVER ASK AGAIN.'或者'不在提示'，且拒绝权限。
         * 这时候，需要跳转到设置界面去，让用户手动开启。
         */
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(getActivity()).build().show();
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
        mTimerTask.cancel();
        mTimer.cancel();
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


}
