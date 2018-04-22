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


/**
 * A simple {@link Fragment} subclass.
 */
public class FlashFragment extends Fragment {

    private CameraManager mCameraManager;
    private Context mContext;
    private FloatingActionButton mFab;
    private boolean isFlashOn = false;

    private final int PERMISSION_REQUEST_CAMERA = 1;

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
        if (mContext == null) {
            mContext = getContext();
        }
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        }else {
            openTorch(true);
        }
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
                } else {
                    mFab.setImageDrawable(getResources().getDrawable(R.mipmap.ic_flash_on));
                    Toast.makeText(getContext(), "已打开闪光灯", Toast.LENGTH_SHORT).show();
                    isFlashOn = true;
                    openTorch(true);
                }
            }
        });
    }

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CAMERA){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                openTorch(true);
            }else{
                Toast.makeText(mContext, "权限被拒绝，我们需要你的相机权限来打开闪光灯", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
