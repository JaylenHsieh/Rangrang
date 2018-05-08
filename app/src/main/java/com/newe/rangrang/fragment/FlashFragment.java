package com.newe.rangrang.fragment;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.newe.rangrang.R;
import com.newe.rangrang.db.Constance;
import com.newe.rangrang.permission.PermissionManager;
import com.newe.rangrang.utils.ToastUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;


/**
 * 闪光灯功能的Fragment
 *
 * @author Jaylen Hsieh
 * @date 2018/04/27.
 */
public class FlashFragment extends Fragment implements EasyPermissions.PermissionCallbacks,
        NumberPicker.OnValueChangeListener,
        NumberPicker.OnScrollListener {

    @BindView(R.id.surface_view)
    SurfaceView mSurfaceView;
    @BindView(R.id.fab_flash)
    FloatingActionButton mFab;
    @BindView(R.id.circleImageView)
    CircleImageView mCircleImageView;
    @BindView(R.id.secondPicker)
    NumberPicker mSecondPicker;
    Unbinder unbinder;

    private CameraManager mCameraManager;
    private Context mContext;
    private boolean isFlashOn = false;
    private boolean isGlittering = false;
    private SurfaceHolder mSurfaceViewHolder;
    private Handler mHandler;
    private String mCameraId;
    private ImageReader mImageReader;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mSession;
    private Handler mainHandler;

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;



    public FlashFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_flash, container, false);
        // 获取 context
        if (mContext == null) {
            mContext = getContext();
        }
        checkCameraPermission();
        unbinder = ButterKnife.bind(this, view);
        //initSurfaceView(view);
        initPicker();
        return view;
    }

    /**
     * 设置数字的最大值最小值，已经滑动事件
     */
    private void initPicker() {
        mSecondPicker.setOnValueChangedListener(this);
        mSecondPicker.setMaxValue(50);
        mSecondPicker.setMinValue(5);
        // 从SharedPreferences里面读取上次设定的值
        mPreferences = mContext.getSharedPreferences("data",Context.MODE_PRIVATE);
        mSecondPicker.setValue(mPreferences.getInt("second",15));
    }


    @Override
    public void onResume() {
        super.onResume();

        // 获取相机管理器
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CountDownTimer mCountDownTimer = new CountDownTimer(mPreferences.getInt("second",15)*1000, 500) {
                    @Override
                    public void onTick(long l) {
                        isGlittering = !isGlittering;
                        openTorch(isGlittering);
                    }

                    @Override
                    public void onFinish() {
                        isGlittering = false;
                        openTorch(isGlittering);
                        // 计时结束销毁，避免占用硬件
                        if (mCameraManager != null) {
                            try {
                                mCameraManager.setTorchMode("0", false);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }
                        initSurfaceView(view);
                    }
                };
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    /**
     * 获取到选中的值
     *
     * @param picker
     * @param oldVal
     * @param newVal
     */
    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        mEditor = mContext.getSharedPreferences("data",Context.MODE_PRIVATE).edit();
        mEditor.putInt("second",newVal);
        mEditor.apply();
    }

    private void initSurfaceView(View view) {
        mSurfaceViewHolder = mSurfaceView.getHolder();
        mSurfaceViewHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                initCameraAndPreview();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                // 释放 camera
                if (mCameraDevice != null) {
                    mCameraDevice.close();
                    mCameraDevice = null;
                }
            }
        });
    }

    @TargetApi(19)
    private void initCameraAndPreview() {
        HandlerThread handlerThread = new HandlerThread("My First Camera2");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
        //用来处理 ui 线程的handler，即 ui 线程
        mainHandler = new Handler(Looper.getMainLooper());
        try {
            //前置摄像头：LENS_FACING_BACK，
            //后置摄像头：LENS_FACING_FRONT。
            mCameraId = "" + CameraCharacteristics.LENS_FACING_FRONT;
            mImageReader = ImageReader.newInstance(mSurfaceView.getWidth(), mSurfaceView.getHeight(), ImageFormat.JPEG, 7);
            //这里必须传入mainHandler，因为涉及到了 ui 操作
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mainHandler);
            mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
            checkCameraPermission();
            mCameraManager.openCamera(mCameraId, deviceStateCallback, mHandler);
        } catch (CameraAccessException e) {
            Toast.makeText(mContext, "Error", Toast.LENGTH_SHORT).show();
        }

    }

    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            //进行相片存储
            mCameraDevice.close();
            Image image = reader.acquireNextImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            //将image对象转化为byte，再转化为bitmap
            buffer.get(bytes);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bitmap != null) {
                mCircleImageView.setImageBitmap(bitmap);
            }
        }
    };

    private CameraDevice.StateCallback deviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            try {
                takePreview();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Toast.makeText(mContext, "打开摄像头失败", Toast.LENGTH_SHORT).show();
        }
    };

    public void takePreview() throws CameraAccessException {
        mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        mPreviewBuilder.addTarget(mSurfaceViewHolder.getSurface());
        mCameraDevice.createCaptureSession(Arrays.asList(mSurfaceViewHolder.getSurface(), mImageReader.getSurface()), mSessionPreviewStateCallback, mHandler);
    }

    private CameraCaptureSession.StateCallback mSessionPreviewStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            mSession = session;
            //配置完毕开始预览
            try {
                /**
                 * 设置你需要配置的参数
                 */
                //自动对焦
                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                //打开闪光灯
                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                //无限次的重复获取图像
                mSession.setRepeatingRequest(mPreviewBuilder.build(), null, mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Toast.makeText(mContext, "配置失败", Toast.LENGTH_SHORT).show();
        }
    };
    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            mSession = session;
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            mSession = session;
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
        }
    };

    public void takePicture() {
        try {
            //用来设置拍照请求的request
            CaptureRequest.Builder captureRequestBuilder
                    = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureRequestBuilder.addTarget(mImageReader.getSurface());
            // 自动对焦
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // 自动曝光
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics(mCameraId);
            //使图片做顺时针旋转
            captureRequestBuilder
                    .set(CaptureRequest.JPEG_ORIENTATION,
                            getJpegOrientation(cameraCharacteristics, rotation));
            CaptureRequest mCaptureRequest = captureRequestBuilder.build();
            mSession.capture(mCaptureRequest, null, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //获取图片应该旋转的角度，使图片竖直
    public int getOrientation(int rotation) {
        switch (rotation) {
            case Surface.ROTATION_0:
                return 90;
            case Surface.ROTATION_90:
                return 0;
            case Surface.ROTATION_180:
                return 270;
            case Surface.ROTATION_270:
                return 180;
            default:
                return 0;
        }
    }

    //获取图片应该旋转的角度，使图片竖直
    private int getJpegOrientation(CameraCharacteristics c, int deviceOrientation) {
        if (deviceOrientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            return 0;
        }

        int sensorOrientation = c.get(CameraCharacteristics.SENSOR_ORIENTATION);

        // Round device orientation to a multiple of 90
        deviceOrientation = (deviceOrientation + 45) / 90 * 90;

        // LENS_FACING相对于设备屏幕的方向,LENS_FACING_FRONT相机设备面向与设备屏幕相同的方向
        boolean facingFront = c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT;
        if (facingFront) {
            deviceOrientation = -deviceOrientation;
        }

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        int jpegOrientation = (sensorOrientation + deviceOrientation + 360) % 360;

        return jpegOrientation;
    }

    @Override
    public void onScrollStateChange(NumberPicker view, int scrollState) {
        switch (scrollState) {
            case SCROLL_STATE_FLING:
                Toast.makeText(mContext, "后续滑动", Toast.LENGTH_SHORT).show();
                break;
            case SCROLL_STATE_IDLE:
                Toast.makeText(mContext, "不滑动", Toast.LENGTH_SHORT).show();
                break;
            case SCROLL_STATE_TOUCH_SCROLL:
                Toast.makeText(mContext, "滑动中", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }
}
