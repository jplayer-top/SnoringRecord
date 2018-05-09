package top.jplayer.audio;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.czt.mp3recorder.MP3Recorder;
import com.shuyu.waveview.AudioPlayer;
import com.shuyu.waveview.AudioWaveView;
import com.shuyu.waveview.FileUtils;
import com.vondear.rxtools.RxVibrateTool;
import com.vondear.rxtools.activity.ActivityWebView;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import top.jplayer.audio.bean.RecordSleepBean;
import top.jplayer.audio.dialog.CurrentRecordDialog;
import top.jplayer.audio.dialog.ValueSureDialog;
import top.jplayer.audio.utils.AndroidScheduler;
import top.jplayer.audio.utils.CustomMp3Recorder;
import top.jplayer.audio.utils.DateUtils;
import top.jplayer.audio.utils.RecordDaoUtil;
import top.jplayer.audio.utils.ScreenUtils;
import top.jplayer.audio.utils.SizeUtils;
import top.jplayer.audio.view.CompassServant;

public class MainActivity extends AppCompatActivity implements CompassServant.ServantListener {
    @BindView(R.id.audioWave)
    AudioWaveView audioWave;
    @BindView(R.id.record)
    Button record;
    @BindView(R.id.stop)
    Button stop;
    @BindView(R.id.compass_servant)
    CompassServant compass_servant;

    private RecordSleepBean mRecordSleepBean;
    @BindView(R.id.reset)
    Button reset;
    @BindView(R.id.wavePlay)
    Button wavePlay;
    @BindView(R.id.recordPause)
    Button recordPause;
    @BindView(R.id.btnValue)
    Button btnValue;
    @BindView(R.id.webView)
    Button webView;
    @BindView(R.id.recordShow)
    Button recordShow;
    @BindView(R.id.rootView)
    ViewGroup rootView;

    private List<Integer> integerList;
    private List<Integer> countSnoring;
    CustomMp3Recorder mRecorder;
    AudioPlayer audioPlayer;

    String filePath;

    boolean mIsRecord = false;

    boolean mIsPlay = false;

    int duration;
    int curPosition;
    private Handler mUIHandler;
    private Disposable subscribe;
    private List<Integer> countStatus;
    private Disposable subscribe1;
    private Disposable subscribe2;
    private Camera mCamera;
    private Disposable subscribe3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mUIHandler = new Handler();
        compass_servant.setServantListener(this);
        compass_servant.setPointerDecibel(118);
        integerList = new ArrayList<>();
        countStatus = new ArrayList<>();
        countSnoring = new ArrayList<>();
        mRecordSleepBean = new RecordSleepBean();
        AndPermission.with(this)
                .permission(Permission.WRITE_EXTERNAL_STORAGE, Permission.RECORD_AUDIO, Permission.CAMERA)
                .onGranted(new Action() {
                    @Override
                    public void onAction(List<String> permissions) {
                    }
                })
                .onDenied(new Action() {
                    @Override
                    public void onAction(@NonNull List<String> permissions) {
                        AndPermission.hasAlwaysDeniedPermission(MainActivity.this, permissions);
                    }
                })
                .start();
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onStart() {
        super.onStart();
        resolveNormalUI();
        audioPlayer = new AudioPlayer(this, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case AudioPlayer.HANDLER_CUR_TIME://更新的时间
                        curPosition = (int) msg.obj;
                        break;
                    case AudioPlayer.HANDLER_COMPLETE://播放结束
                        mIsPlay = false;
                        break;
                    case AudioPlayer.HANDLER_PREPARED://播放开始
                        duration = (int) msg.obj;
                        break;
                    case AudioPlayer.HANDLER_ERROR://播放错误
                        resolveResetPlay();
                        break;
                }

            }
        });
    }

    private int setValue = 60;

    @OnClick({R.id.btnValue, R.id.recordShow, R.id.webView, R.id.record, R.id.stop, R.id.reset, R.id.wavePlay, R.id
            .recordPause})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnValue:
                ValueSureDialog dialog = new ValueSureDialog();
                dialog.setSureListener(new ValueSureDialog.SureListener() {
                    @Override
                    public void onSureListener(int value) {
                        setValue = value;
                        btnValue.setText(String.format(Locale.CHINA, "设置阀值（当前 %d）", setValue));
                    }
                });
                dialog.show(getSupportFragmentManager(), "value");
                break;
            case R.id.webView:
                Intent intent = new Intent(this, ActivityWebView.class);
                Bundle value = new Bundle();
                value.putString("url", "http://www.baidu.com");
                intent.putExtra("bundle", value);
                startActivity(intent);
                break;
            case R.id.recordShow:
                startActivity(new Intent(this, RecordSleepActivity.class));
                break;
            case R.id.record:
                resolveRecord();
                break;
            case R.id.stop:
                resolveStopRecord();
                break;
            case R.id.reset:
                resolveResetPlay();
            case R.id.wavePlay:
                resolvePlayWaveRecord();
            case R.id.recordPause:
                resolvePause();
                break;
        }
    }

    int countOnce = 0;
    int countMore = 0;
    int count = 0;
    /**
     * 是否处于打鼾状态
     */
    boolean isStartVibrate = false;
    boolean isFlash = false;

    /**
     * 开始录音
     */
    @SuppressLint("HandlerLeak")
    public void resolveRecord() {
        filePath = FileUtils.getAppPath();
        File file = new File(filePath);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Toast.makeText(this, "创建文件失败", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        int offset = SizeUtils.dp2px(1);
        filePath = FileUtils.getAppPath() + UUID.randomUUID().toString() + ".mp3";
        mRecorder = new CustomMp3Recorder(new File(filePath));
        int size = ScreenUtils.getScreenWidth() / offset;//控件默认的间隔是1
        mRecorder.setDataList(audioWave.getRecList(), size);


        mRecorder.setErrorHandler(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MP3Recorder.ERROR_TYPE) {
                    Toast.makeText(MainActivity.this, "没有麦克风权限", Toast.LENGTH_SHORT).show();
                    resolveError();
                }
            }
        });
        integerList.clear();
        countSnoring.clear();
        /**
         * 打鼾状态检测，一秒轮循检测，监测机制
         * 1.大于60分贝的话记录，一秒内超过某分贝即为打鼾(一秒内记录值大约为20次，超过三次，低于十五次即为打鼾，否则为其他状态)
         * 2.
         *
         */

        subscribe = Observable.interval(1, TimeUnit.SECONDS).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                if (integerList.size() > 0) {
                    countOnce = 0;
                    countMore = 0;
                    count = 0;
                    Observable.fromIterable(integerList).subscribe(new Consumer<Integer>() {
                        @Override
                        public void accept(Integer integer) throws Exception {
                            if (integer > setValue) {
                                countOnce += integer;
                                ++count;
                            }
                            countMore += integer;
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {

                        }
                    });
                    Log.e("一秒数据", Arrays.toString(integerList.toArray()));

                    int iOnce = countOnce / 3;
                    int iMore = countMore / integerList.size();


                    if (iOnce > setValue) {
                        countStatus.add(iOnce);
                    }
                    Log.e("Count", count + "--");
                    if (isStartVibrate && iMore > setValue && count <= (16 >= integerList.size() ? integerList.size()
                            : 16) && !isPause) {
                        countSnoring.add(iMore);
                        RxVibrateTool.vibrateOnce(MainActivity.this, 300);

                    }
                    integerList.clear();
                }
            }
        });

        countStatus.clear();
        /**
         * 检测是否处于打鼾状态
         */
        subscribe1 = Observable.interval(10, TimeUnit.SECONDS).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                /**
                 *
                 */
                if (5 <= countStatus.size()) {
                    countSnoring.add(countStatus.size());
                    if (!isStartVibrate && !isPause) {
                        RxVibrateTool.vibrateOnce(MainActivity.this, 1000);
                    }
                    countStatus.clear();
                    isStartVibrate = true;
                } else {
                    isStartVibrate = false;
                }
            }
        });
        /**
         * 开启超时检测打鼾，一分钟后  一定时间内开启，否则关闭打鼾状态
         */
        subscribe2 = Observable.timer(1, TimeUnit.MINUTES)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidScheduler.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        isStartVibrate = false;
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
        subscribe3 = Flowable.interval(500, TimeUnit.MILLISECONDS).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                if (countSnoring.size() >= 15) {
                    isFlash = !isFlash;
                    if (isFlash) {
                        openCameraFlash();
                    } else {
                        closeCameraFlash();
                    }
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {

            }
        });
        try {
            mRecorder.start(new CustomMp3Recorder.VolumeListener() {
                @Override
                public void onVolumeListener(final Double volume) {
                    mUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            int value = volume.intValue();
                            if (!isPause) {
                                compass_servant.setPointerDecibel(value);
                                integerList.add(value);
                            }
                        }
                    });
                }
            });
            audioWave.startView();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "权限未获取", Toast.LENGTH_SHORT).show();
            resolveError();
            return;
        }
        resolveRecordUI();
        mIsRecord = true;
        mStartDate = new Date();
        mRecordSleepBean.startTime = DateUtils.getSpecifyDate(mStartDate, DateUtils.FORMAT_HH_MM_SS);
    }

    Date mStartDate;
    Date mEndDate;

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsRecord) {
            resolveStopRecord();
        }
    }

    /**
     * 停止录音
     */
    private void resolveStopRecord() {
        resolveStopUI();
        if (!subscribe.isDisposed()) {
            subscribe.dispose();
        }
        if (!subscribe1.isDisposed()) {
            subscribe1.dispose();
        }
        if (!subscribe2.isDisposed()) {
            subscribe2.dispose();
        }
        if (!subscribe3.isDisposed()) {
            subscribe3.dispose();
        }
        isStartVibrate = false;
        if (mRecorder != null && mRecorder.isRecording()) {
            mRecorder.setPause(false);
            mRecorder.stop();
            audioWave.stopView();
        }
        mIsRecord = false;
        recordPause.setText("暂停");

        mEndDate = new Date();

        mRecordSleepBean.endTime = DateUtils.getSpecifyDate(mEndDate, DateUtils.FORMAT_HH_MM_SS);

        mRecordSleepBean.day = DateUtils.getCurrentDate();

        int cCount = countSnoring.size();
        mRecordSleepBean.sleepSnoring = cCount + " 次";

        long dur = mEndDate.getTime() - mStartDate.getTime();
        mRecordSleepBean.sleepTime = DateUtils.formatLongToTimeStr(dur);
        float v = (float) cCount * 100000 / dur;
        mRecordSleepBean.account = String.format(Locale.CHINA, "%.2f%%", v);

        CurrentRecordDialog dialog = new CurrentRecordDialog();
        Bundle arguments = new Bundle();
        arguments.putParcelable("record", mRecordSleepBean);
        dialog.setArguments(arguments);
        dialog.show(getSupportFragmentManager(), "current");


        RecordDaoUtil recordDaoUtil = new RecordDaoUtil(this);
        RecordSleepBean recordBean = null;
        for (RecordSleepBean recordSleepBean : recordDaoUtil.queryAllbean()) {
            if (TextUtils.equals(recordSleepBean.day, mRecordSleepBean.day)) {
                recordBean = recordSleepBean;
            }
        }
        if (recordBean != null) {
            recordBean.sleepSnoring = mRecordSleepBean.sleepSnoring;
            recordBean.startTime = mRecordSleepBean.startTime;
            recordBean.endTime = mRecordSleepBean.endTime;
            recordBean.day = mRecordSleepBean.day;
            recordBean.sleepTime = mRecordSleepBean.sleepTime;
            recordBean.account = mRecordSleepBean.account;
            recordDaoUtil.updatebean(recordBean);
        } else {
            recordDaoUtil.insertbean(mRecordSleepBean);
        }
        countSnoring.clear();
    }

    /**
     * 录音异常
     */
    private void resolveError() {
        resolveNormalUI();
        FileUtils.deleteFile(filePath);
        filePath = "";
        if (mRecorder != null && mRecorder.isRecording()) {
            mRecorder.stop();
            audioWave.stopView();
        }
    }

    /**
     * 播放
     */
    private void resolvePlayWaveRecord() {
        if (TextUtils.isEmpty(filePath) || !new File(filePath).exists()) {
            Toast.makeText(this, "记录已清空", Toast.LENGTH_SHORT).show();
            return;
        }
        resolvePlayUI();
        Intent intent = new Intent(this, WavePlayActivity.class);
        intent.putExtra("uri", filePath);
        startActivity(intent);
    }

    /**
     * 重置
     */
    private void resolveResetPlay() {
        filePath = "";
        if (mIsPlay) {
            mIsPlay = false;
            audioPlayer.pause();
        }
        resolveNormalUI();
    }

    boolean isPause = false;

    /**
     * 暂停
     */
    private void resolvePause() {
        if (!mIsRecord)
            return;
        resolvePauseUI();
        if (mRecorder.isPause()) {
            resolveRecordUI();
            audioWave.setPause(false);
            mRecorder.setPause(false);
            recordPause.setText("暂停");
            isPause = false;
        } else {
            audioWave.setPause(true);
            mRecorder.setPause(true);
            recordPause.setText("继续");
            isPause = true;
        }
    }

    public void openCameraFlash() {
        try {
            mCamera = Camera.open();
            Camera.Parameters mParameters;
            mParameters = mCamera.getParameters();
            mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(mParameters);
        } catch (Exception ex) {
        }

    }

    public void closeCameraFlash() {
        try {
            if (mCamera != null) {
                Camera.Parameters mParameters;
                mParameters = mCamera.getParameters();
                mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(mParameters);
                mCamera.release();
            }
        } catch (Exception ex) {
        }
    }

    private long mBackPressed;
    private static final int TIME_INTERVAL = 2000;

    @Override
    public void onBackPressed() {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed();
            return;
        } else {
            Toast.makeText(getBaseContext(), "再次点击返回键退出", Toast.LENGTH_SHORT).show();
        }
        mBackPressed = System.currentTimeMillis();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!subscribe.isDisposed()) {
            subscribe.dispose();
        }
        if (!subscribe1.isDisposed()) {
            subscribe1.dispose();
        }
        if (!subscribe2.isDisposed()) {
            subscribe2.dispose();
        }
    }

    private void resolveNormalUI() {
        record.setEnabled(true);
        recordPause.setEnabled(false);
        stop.setEnabled(false);
        wavePlay.setEnabled(false);
        reset.setEnabled(false);
    }

    private void resolveRecordUI() {
        record.setEnabled(false);
        recordPause.setEnabled(true);
        stop.setEnabled(true);
        wavePlay.setEnabled(false);
        reset.setEnabled(false);
        recordShow.setEnabled(false);
        webView.setEnabled(false);
        btnValue.setEnabled(false);
    }

    private void resolveStopUI() {
        record.setEnabled(true);
        stop.setEnabled(false);
        recordPause.setEnabled(false);
        wavePlay.setEnabled(true);
        reset.setEnabled(true);
        recordShow.setEnabled(true);
        webView.setEnabled(true);
        btnValue.setEnabled(true);
    }

    private void resolvePlayUI() {
        record.setEnabled(false);
        stop.setEnabled(false);
        recordPause.setEnabled(false);
        wavePlay.setEnabled(true);
        reset.setEnabled(true);
    }

    private void resolvePauseUI() {
        record.setEnabled(false);
        recordPause.setEnabled(true);
        stop.setEnabled(false);
        wavePlay.setEnabled(false);
        reset.setEnabled(false);
    }


    @Override
    public void startTension() {

    }
}
