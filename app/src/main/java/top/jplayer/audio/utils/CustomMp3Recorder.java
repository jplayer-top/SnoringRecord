package top.jplayer.audio.utils;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Handler;
import android.os.Process;
import android.util.Log;

import com.BaseRecorder;
import com.czt.mp3recorder.DataEncodeThread;
import com.czt.mp3recorder.MP3Recorder;
import com.czt.mp3recorder.PCMFormat;
import com.czt.mp3recorder.util.LameUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Administrator on 2018/5/5.
 * 自定义监听分贝器
 */

public class CustomMp3Recorder extends BaseRecorder {
    private static final int DEFAULT_AUDIO_SOURCE = 1;
    private static final int DEFAULT_SAMPLING_RATE = 44100;
    private static final int DEFAULT_CHANNEL_CONFIG = 16;
    private static final PCMFormat DEFAULT_AUDIO_FORMAT;
    private static final int DEFAULT_LAME_MP3_QUALITY = 7;
    private static final int DEFAULT_LAME_IN_CHANNEL = 1;
    private static final int DEFAULT_LAME_MP3_BIT_RATE = 32;
    private static final int FRAME_COUNT = 160;
    public static final int ERROR_TYPE = 22;
    private AudioRecord mAudioRecord = null;
    private DataEncodeThread mEncodeThread;
    private File mRecordFile;
    private ArrayList<Short> dataList;
    private Handler errorHandler;
    private short[] mPCMBuffer;
    private boolean mIsRecording = false;
    private boolean mSendError;
    private boolean mPause;
    private int mBufferSize;
    private int mMaxSize;
    private int mWaveSpeed = 300;
    private static final int MAX_VOLUME = 2000;
    static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(8000,
            AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);

    public CustomMp3Recorder(File recordFile) {
        this.mRecordFile = recordFile;
    }

    public interface VolumeListener {
        void onVolumeListener(Double volume);
    }

    public VolumeListener listener;

    public void start(final VolumeListener listener) throws IOException {
        if (!this.mIsRecording) {
            this.mIsRecording = true;
            this.initAudioRecorder();
            this.listener = listener;
            try {
                this.mAudioRecord.startRecording();
            } catch (Exception var2) {
                var2.printStackTrace();
            }

            (new Thread() {
                boolean isError = false;

                public void run() {
                    Process.setThreadPriority(-19);
                    while (true) {
                        while (CustomMp3Recorder.this.mIsRecording) {

                            int readSize = mAudioRecord.read(CustomMp3Recorder.this.mPCMBuffer, 0, CustomMp3Recorder.this.mBufferSize);
                            long v = 0;
                            // 将 buffer 内容取出，进行平方和运算
                            for (int i = 0; i < mPCMBuffer.length; i++) {
                                v += mPCMBuffer[i] * mPCMBuffer[i];
                            }
                            // 平方和除以数据总长度，得到音量大小。
                            double mean = v / (double) readSize;
                            double volume = 10 * Math.log10(mean);
                            if (CustomMp3Recorder.this.listener != null) {
                                listener.onVolumeListener(volume);
                            }
                            if (readSize != -3 && readSize != -2) {
                                if (readSize > 0) {
                                    if (!CustomMp3Recorder.this.mPause) {
                                        CustomMp3Recorder.this.mEncodeThread.addTask(CustomMp3Recorder.this.mPCMBuffer, readSize);
                                        CustomMp3Recorder.this.calculateRealVolume(CustomMp3Recorder.this.mPCMBuffer, readSize);
                                        CustomMp3Recorder.this.sendData(CustomMp3Recorder.this.mPCMBuffer, readSize);
                                    }
                                } else if (CustomMp3Recorder.this.errorHandler != null && !CustomMp3Recorder.this.mSendError) {
                                    CustomMp3Recorder.this.mSendError = true;
                                    CustomMp3Recorder.this.errorHandler.sendEmptyMessage(22);
                                    CustomMp3Recorder.this.mIsRecording = false;
                                    this.isError = true;
                                }
                            } else if (CustomMp3Recorder.this.errorHandler != null && !CustomMp3Recorder.this.mSendError) {
                                CustomMp3Recorder.this.mSendError = true;
                                CustomMp3Recorder.this.errorHandler.sendEmptyMessage(22);
                                CustomMp3Recorder.this.mIsRecording = false;
                                this.isError = true;
                            }
                        }

                        try {
                            CustomMp3Recorder.this.mAudioRecord.stop();
                            CustomMp3Recorder.this.mAudioRecord.release();
                            CustomMp3Recorder.this.mAudioRecord = null;
                        } catch (Exception var2) {
                            var2.printStackTrace();
                        }

                        if (this.isError) {
                            CustomMp3Recorder.this.mEncodeThread.sendErrorMessage();
                        } else {
                            CustomMp3Recorder.this.mEncodeThread.sendStopMessage();
                        }

                        return;
                    }
                }
            }).start();
        }
    }

    public int getRealVolume() {
        return this.mVolume;
    }

    public int getVolume() {
        return this.mVolume >= 2000 ? 2000 : this.mVolume;
    }

    public int getMaxVolume() {
        return 2000;
    }

    public void stop() {
        this.mPause = false;
        this.mIsRecording = false;
    }

    public boolean isRecording() {
        return this.mIsRecording;
    }

    private void initAudioRecorder() throws IOException {
        this.mBufferSize = AudioRecord.getMinBufferSize('걄', 16, DEFAULT_AUDIO_FORMAT.getAudioFormat());
        int bytesPerFrame = DEFAULT_AUDIO_FORMAT.getBytesPerFrame();
        int frameSize = this.mBufferSize / bytesPerFrame;
        if (frameSize % 160 != 0) {
            frameSize += 160 - frameSize % 160;
            this.mBufferSize = frameSize * bytesPerFrame;
        }

        this.mAudioRecord = new AudioRecord(1, '걄', 16, DEFAULT_AUDIO_FORMAT.getAudioFormat(), this.mBufferSize);
        this.mPCMBuffer = new short[this.mBufferSize];
        LameUtil.init('걄', 1, '걄', 32, 7);
        this.mEncodeThread = new DataEncodeThread(this.mRecordFile, this.mBufferSize);
        this.mEncodeThread.start();
        this.mAudioRecord.setRecordPositionUpdateListener(this.mEncodeThread, this.mEncodeThread.getHandler());
        this.mAudioRecord.setPositionNotificationPeriod(160);
    }

    private void sendData(short[] shorts, int readSize) {
        if (this.dataList != null) {
            int length = readSize / this.mWaveSpeed;
            short resultMax = 0;
            short i = 0;

            for (short k = 0; i < length; k = (short) (k + this.mWaveSpeed)) {
                short j = k;
                short max = 0;

                for (short min = 1000; j < k + this.mWaveSpeed; ++j) {
                    if (shorts[j] > max) {
                        max = shorts[j];
                        resultMax = max;
                    } else if (shorts[j] < min) {
                        min = shorts[j];
                    }
                }

                if (this.dataList.size() > this.mMaxSize) {
                    this.dataList.remove(0);
                }

                this.dataList.add(Short.valueOf(resultMax));
                ++i;
            }
        }

    }

    public void setDataList(ArrayList<Short> dataList, int maxSize) {
        this.dataList = dataList;
        this.mMaxSize = maxSize;
    }

    public boolean isPause() {
        return this.mPause;
    }

    public void setPause(boolean pause) {
        this.mPause = pause;
    }

    public void setErrorHandler(Handler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public int getWaveSpeed() {
        return this.mWaveSpeed;
    }

    public void setWaveSpeed(int waveSpeed) {
        if (this.mWaveSpeed > 0) {
            this.mWaveSpeed = waveSpeed;
        }
    }

    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else {
                String[] filePaths = file.list();
                String[] var3 = filePaths;
                int var4 = filePaths.length;

                for (int var5 = 0; var5 < var4; ++var5) {
                    String path = var3[var5];
                    deleteFile(filePath + File.separator + path);
                }

                file.delete();
            }
        }

    }

    static {
        DEFAULT_AUDIO_FORMAT = PCMFormat.PCM_16BIT;
    }
}