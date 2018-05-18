package com.newe.rangrang.fragment;


import android.content.Context;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.newe.rangrang.R;
import com.newe.rangrang.db.Constance;
import com.newe.rangrang.permission.PermissionManager;
import com.newe.rangrang.utils.AudioUtils;
import com.newe.rangrang.utils.ToastUtils;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;


/**
 * 出示红牌的fragment
 *
 * @author Jaylen Hsieh
 * @date 2018/04/22.
 */
public class ScreenFragment extends Fragment implements EasyPermissions.PermissionCallbacks, SurfaceHolder.Callback {

    @BindView(R.id.img_background)
    ImageView mImgBackground;
    @BindView(R.id.fab_sound)
    FloatingActionButton mFabSound;
    @BindView(R.id.tv_stop)
    TextView mTvStop;
    Unbinder unbinder;


    private Context mContext;

    /**
     * 默认关闭提示音
     */
    private boolean isTrumpetOn = false;

    /**
     * 默认透明度为0
     */
    private boolean isBackgroundHidden = false;

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

    private CircleImageView mImageView;
    private TextView mTvTime;
    private TextView mBtnStartStop;
    private TextView mBtnPlay;
    private SurfaceView mSurfaceView;

//下面两个成员变量是为了开启线程用的
    private android.os.Handler handler = new android.os.Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            time++;
            mTvTime.setText("已录制" + time + "s");
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_screen, container, false);
        if (mContext == null) {
            mContext = getContext();
        }
        //检查权限与绑定视图
        checkCameraPermission();
        checkStoragePermission();
        mImageView = view.findViewById(R.id.circleImageView_screen);
        mSurfaceView = view.findViewById(R.id.surface_view);
        mBtnStartStop = view.findViewById(R.id.btn_start_stop);
        mBtnPlay = view.findViewById(R.id.btn_play);
        mTvTime = view.findViewById(R.id.tv_time);

        unbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBtnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTvTime.setVisibility(View.VISIBLE);
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
                    mImageView.setVisibility(View.GONE);
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
//设置文件的路径
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
                            mBtnStartStop.setText("Stop");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //stop
                    if (isRecording) {
                        try {
                            handler.removeCallbacks(runnable);
                            mRecorder.stop();
                            mRecorder.reset();
                            mRecorder.release();
                            mRecorder = null;
                            mBtnStartStop.setText("Start");
                            if (camera != null) {
                                camera.release();
                                camera = null;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    isRecording = false;
                }
            }
        });

        mBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTvStop.setVisibility(View.GONE);
                mTvTime.setVisibility(View.GONE);
                isPlayingRecord = true;
                mImageView.setVisibility(View.GONE);
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
        });
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.addCallback(this);
        // setType必须设置，要不出错.
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isRecording) {
            mImageView.setVisibility(View.VISIBLE);
        }
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
            isBackgroundHidden = !isBackgroundHidden;
            setBackgroundHidden(isBackgroundHidden);
        }

        @Override
        public void onFinish() {
            isBackgroundHidden = false;
            setBackgroundHidden(isBackgroundHidden);
        }
    };

    private void setBackgroundHidden(boolean isBackgroundHidden) {
        if (isBackgroundHidden) {
            mImgBackground.setVisibility(View.GONE);
            mTvStop.setTextColor(getResources().getColor(R.color.colorPrimary,getActivity().getTheme()));
        } else {
            mImgBackground.setVisibility(View.VISIBLE);
            mTvStop.setTextColor(getResources().getColor(R.color.white,getActivity().getTheme()));
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
     * @return SD存储路径字符串
     */
    public String getSDPath() {
        File sdDir;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取根目录
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setWindowBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE);
        countDownTimer.cancel();
        unbinder.unbind();
    }

    @OnClick({R.id.fab_sound, R.id.circleImageView_screen})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.fab_sound:
                mTvStop.setVisibility(View.VISIBLE);
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
                    AudioUtils.initPlayer(getContext());
                    isTrumpetOn = true;
                }
                break;
            case R.id.circleImageView_screen:
                break;
            default:
                break;
        }
    }


}
