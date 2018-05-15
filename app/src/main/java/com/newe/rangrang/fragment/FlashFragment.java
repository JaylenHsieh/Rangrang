package com.newe.rangrang.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.newe.rangrang.R;
import com.newe.rangrang.db.Constance;
import com.newe.rangrang.permission.PermissionManager;
import com.newe.rangrang.utils.ToastUtils;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static com.newe.rangrang.fragment.ScreenFragment.getDate;


/**
 * 闪光灯功能的Fragment
 *
 * @author Jaylen Hsieh
 * @date 2018/04/27.
 */
public class FlashFragment extends Fragment implements EasyPermissions.PermissionCallbacks,
        NumberPicker.OnValueChangeListener,
        NumberPicker.OnScrollListener,
        SurfaceHolder.Callback {

    @BindView(R.id.surface_view)
    SurfaceView mSurfaceView;
    @BindView(R.id.fab_flash)
    FloatingActionButton mFab;
    @BindView(R.id.circleImageView)
    CircleImageView mCircleImageView;
    @BindView(R.id.secondPicker)
    NumberPicker mSecondPicker;
    Unbinder unbinder;
    @BindView(R.id.tv_time)
    TextView mTvTime;

    private CameraManager mCameraManager;
    private Context mContext;
    private boolean isFlashOn = false;
    private boolean isGlittering = false;
    private CountDownTimer mCountDownTimer;
    /**
     * 是否正在录像
     */
    private boolean isRecording = false;
    /**
     * 是否正在播放录像
     */
    private boolean isPlayingRecord = false;

    private static final String TAG = "MainActivity";
    private MediaRecorder mRecorder;
    private SurfaceHolder mSurfaceHolder;
    private Camera camera;
    private MediaPlayer mediaPlayer;
    private String path;
    private int time = 0;

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            time++;
            mTvTime.setText("已录制" + time + "s");
            handler.postDelayed(this, 1000);
        }
    };

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
        SharedPreferences mPreferences = mContext.getSharedPreferences("data", Context.MODE_PRIVATE);
        mSecondPicker.setValue(mPreferences.getInt("second", 15));
    }


    @Override
    public void onResume() {
        super.onResume();
        //获取sharedpreference
        mCountDownTimer = new CountDownTimer(
                mContext.getSharedPreferences("data", Context.MODE_PRIVATE).getInt("second", 15) * 1000, 500) {
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
            }
        };

        // 获取相机管理器
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                mCountDownTimer.cancel();
                mCameraManager.setTorchMode("0", false);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 控制闪光灯的打开和关闭
     *
     * @param enable 开关
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
        /*
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
        if (mCameraManager != null) {
            try {
                mCountDownTimer.cancel();
                mCameraManager.setTorchMode("0", false);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
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
        SharedPreferences.Editor mEditor = mContext.getSharedPreferences("data", Context.MODE_PRIVATE).edit();
        mEditor.putInt("second", newVal);
        mEditor.apply();
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

    private void startRecord() {
        if (isPlayingRecord) {
            if (mediaPlayer != null) {
                isPlayingRecord = false;
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
        if (!isRecording) {
            handler.postDelayed(runnable, 1000);
            if (mRecorder == null) {
                mRecorder = new MediaRecorder();
            }

            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            if (camera != null) {
                camera.setDisplayOrientation(90);
                camera.unlock();
                mRecorder.setCamera(camera);
            }

            try {
                // 这两项需要放在setOutputFormat之前
                mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

                // Set output file format
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

                // 这两项需要放在setOutputFormat之后
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);

                mRecorder.setVideoSize(640, 480);
                mRecorder.setVideoFrameRate(30);
                mRecorder.setVideoEncodingBitRate(3 * 1024 * 1024);
                mRecorder.setOrientationHint(90);
                //设置记录会话的最大持续时间（毫秒）
                mRecorder.setMaxDuration(30 * 1000);
                mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

                path = getSDPath();
                if (path != null) {
                    File dir = new File(path + "/Rangrang");
                    if (!dir.exists() && !dir.mkdir()) {
                        throw new IllegalStateException("目录不存在，且无法成功创建");
                    }
                    path = dir + "/" + getDate() + ".mp4";
                    mRecorder.setOutputFile(path);
                    mRecorder.prepare();
                    mRecorder.start();
                    isRecording = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //stop
            try {
                handler.removeCallbacks(runnable);
                mRecorder.stop();
                mRecorder.reset();
                mRecorder.release();
                mRecorder = null;
                if (camera != null) {
                    camera.release();
                    camera = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            isRecording = false;
        }
    }

    private void startPlay() {
        isPlayingRecord = true;
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        mediaPlayer.reset();
        if (path != null) {
            Uri uri = Uri.parse(path);
            mediaPlayer = MediaPlayer.create(getContext(), uri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDisplay(mSurfaceHolder);
            try {
                mediaPlayer.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaPlayer.start();
            ScaleAnimation scaleAnimation = (ScaleAnimation) AnimationUtils.loadAnimation(getContext(), R.anim.scale_larger);
            mSurfaceView.startAnimation(scaleAnimation);
        } else {
            return;
        }
    }

    /**
     * 获取系统时间
     *
     * @return 系统时间字符串
     */
    public static String getDate() {
        Calendar ca = Calendar.getInstance();
        int year = ca.get(Calendar.YEAR);           // 获取年份
        int month = ca.get(Calendar.MONTH);         // 获取月份
        int day = ca.get(Calendar.DATE);            // 获取日
        int minute = ca.get(Calendar.MINUTE);       // 分
        int hour = ca.get(Calendar.HOUR);           // 小时
        int second = ca.get(Calendar.SECOND);       // 秒

        String date = "" + year + (month + 1) + day + hour + minute + second;
        Log.d(TAG, "date:" + date);

        return date;
    }

    /**
     * 获取SD path
     *
     * @return SD路径，null表示sd卡不存在
     */
    public String getSDPath() {
        File sdDir;
        // 判断sd卡是否存在
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            // 获取根目录
            sdDir = Environment.getExternalStorageDirectory();
            return sdDir.toString();
        }
        return null;
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mSurfaceHolder = surfaceHolder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        // 将holder，这个holder为开始在onCreate里面取得的holder，将它赋给mSurfaceHolder
        mSurfaceHolder = surfaceHolder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mSurfaceView = null;
        mSurfaceHolder = null;
        handler.removeCallbacks(runnable);
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
            Log.d(TAG, "surfaceDestroyed release mRecorder");
        }
        if (camera != null) {
            camera.release();
            camera = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}

