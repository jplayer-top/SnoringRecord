package top.jplayer.audio;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import top.jplayer.audio.bean.RecordSleepBean;
import top.jplayer.audio.dialog.CurrentRecordDialog;
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
    private List<Integer> countList;
    private Disposable subscribe1;
    private Disposable subscribe2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mUIHandler = new Handler();
        compass_servant.setServantListener(this);
        compass_servant.setPointerDecibel(118);
        integerList = new ArrayList<>();
        countList = new ArrayList<>();
        countSnoring = new ArrayList<>();
        mRecordSleepBean = new RecordSleepBean();
        mRecordSleepBean.account = "account";
        AndPermission.with(this)
                .permission(Permission.WRITE_EXTERNAL_STORAGE, Permission.RECORD_AUDIO)
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


    @OnClick({R.id.recordShow, R.id.webView, R.id.record, R.id.stop, R.id.reset, R.id.wavePlay, R.id.recordPause})
    public void onClick(View view) {
        switch (view.getId()) {
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

    int count = 0;
    int countQuick = 0;
    boolean isStartVibrate = false;

    /**
     * 开始录音
     */
    @SuppressLint("HandlerLeak")
    private void resolveRecord() {
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
                    count = 0;
                    countQuick = 0;
                    Observable.fromIterable(integerList).subscribe(new Consumer<Integer>() {
                        @Override
                        public void accept(Integer integer) throws Exception {
                            if (integer > 60) {
                                count += integer;
                            }
                            countQuick += integer;
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {

                        }
                    });
                    Log.e("asdasd", Arrays.toString(integerList.toArray()));
                    int i = count / 3;
                    int iQuick = countQuick / integerList.size();
                    if (i > 60) {
                        countList.add(i);
                    }
                    if (isStartVibrate && iQuick > 60 && iQuick < 70) {
                        countSnoring.add(iQuick);
                        RxVibrateTool.vibrateOnce(MainActivity.this, 300);
                    }
                    integerList.clear();
                }
            }
        });
        countList.clear();
        /**
         * 检测是否处于打鼾状态
         */
        subscribe1 = Observable.interval(10, TimeUnit.SECONDS).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                /**
                 *
                 */
                if (5 <= countList.size()) {
                    isStartVibrate = true;
                    countSnoring.add(countList.size());
                    RxVibrateTool.vibrateOnce(MainActivity.this, 1000);
                    countList.clear();
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
        try {
            mRecorder.start(new CustomMp3Recorder.VolumeListener() {
                @Override
                public void onVolumeListener(final Double volume) {
                    mUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            int value = volume.intValue();
                            compass_servant.setPointerDecibel(value);
                            integerList.add(value);
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

        mRecordSleepBean.sleepSnoring = countSnoring.size() + " 次";

        mRecordSleepBean.sleepTime = DateUtils.formatLongToTimeStr(mEndDate.getTime() - mStartDate.getTime());

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
        } else {
            audioWave.setPause(true);
            mRecorder.setPause(true);
            recordPause.setText("继续");
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
    }

    private void resolveStopUI() {
        record.setEnabled(true);
        stop.setEnabled(false);
        recordPause.setEnabled(false);
        wavePlay.setEnabled(true);
        reset.setEnabled(true);
        recordShow.setEnabled(true);
        webView.setEnabled(true);
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
