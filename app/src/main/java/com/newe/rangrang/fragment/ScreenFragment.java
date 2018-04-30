package com.newe.rangrang.fragment;


import android.annotation.TargetApi;
import android.content.Context;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
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
import butterknife.OnClick;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;


/**
 * A simple {@link Fragment} subclass.
 */
public class ScreenFragment extends Fragment implements EasyPermissions.PermissionCallbacks {

    @BindView(R.id.img_background)
    ImageView mImgBackground;
    @BindView(R.id.fab_sound)
    FloatingActionButton mFabSound;
    @BindView(R.id.circleImageView)
    CircleImageView img_show;

    @BindView(R.id.surface_view)
    SurfaceView mSurfaceView;

    @BindView(R.id.btn_capture)
    TextView mBtnCapture;
    Unbinder unbinder;

    private Context mContext;
    private CameraManager mCameraManager;
    private SurfaceHolder mSurfaceViewHolder;
    private Handler mHandler;
    private String mCameraId;
    private ImageReader mImageReader;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mSession;
    private Handler mainHandler;

    /**
     * 默认关闭提示音
     */
    private boolean isTrumpetOn = false;

    /**
     * 默认透明度为0
     */
    private boolean isAlpha = false;

    public ScreenFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_screen, container, false);
        if (mContext == null) {
            mContext = getContext();
        }
        unbinder = ButterKnife.bind(this, view);
        checkCameraPermission();
        initSurfaceView(view);

        return view;
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
            mCameraId = "" + CameraCharacteristics.LENS_FACING_BACK;
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
                img_show.setImageBitmap(bitmap);
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


    /**
     * 检查读写权限
     */
    private void checkStoragePermission() {
        boolean result = PermissionManager.checkPermission(getActivity(), Constance.PERMS_WRITE);
        if (!result) {
            PermissionManager.requestPermission(getActivity(), Constance.WRITE_PERMISSION_TIP, Constance.WRITE_PERMISSION_CODE, Constance.PERMS_WRITE);
        }
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

    private void setWindowBrightness(float brightness) {
        Window window = getActivity().getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = brightness / 255.0f;
        window.setAttributes(lp);
    }

    CountDownTimer countDownTimer = new CountDownTimer(Long.MAX_VALUE, 300) {
        @Override
        public void onTick(long millisUntilFinished) {
            isAlpha = !isAlpha;
            setAlpha(isAlpha);
        }

        @Override
        public void onFinish() {
            isAlpha = false;
            setAlpha(isAlpha);
        }
    };

    private void setAlpha(boolean isAlpha) {
        if (isAlpha) {
            mImgBackground.setAlpha(255);
        } else {
            mImgBackground.setAlpha(0);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setWindowBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE);
        unbinder.unbind();
    }

    @OnClick({R.id.fab_sound, R.id.circleImageView, R.id.btn_capture})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.fab_sound:
                checkStoragePermission();

                if (isTrumpetOn) {
                    // 点击后，如果扬声器是打开的，关闭提示音，图标显示为喇叭关闭，提示用户已关闭提示音
                    mFabSound.setImageDrawable(getResources().getDrawable(R.mipmap.ic_volume_off_white_24dp));
                    Toast.makeText(getContext(), "已关闭提示音，屏幕亮度已恢复正常", Toast.LENGTH_SHORT).show();
                    // 取消屏幕最亮模式
                    setWindowBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE);
                    countDownTimer.cancel();
                    isTrumpetOn = false;
                } else {
                    mFabSound.setImageDrawable(getResources().getDrawable(R.mipmap.ic_volume_up_white_24dp));
                    Toast.makeText(getContext(), "已打开提示音,屏幕亮度已调节为最高", Toast.LENGTH_SHORT).show();
                    // 调节屏幕为最亮模式
                    setWindowBrightness(255);
                    // 倒计时开始
                    countDownTimer.start();
                    isTrumpetOn = true;
                }
                break;
            case R.id.circleImageView:
                break;
            case R.id.btn_capture:
                takePicture();
                break;
            default:
                break;
        }
    }
}
