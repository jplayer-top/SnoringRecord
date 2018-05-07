package top.jplayer.audio.dialog;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import top.jplayer.audio.R;
import top.jplayer.audio.bean.RecordSleepBean;
import top.jplayer.audio.utils.ScreenUtils;
import top.jplayer.audio.utils.SizeUtils;

/**
 * Created by Obl on 2018/5/7.
 * top.jplayer.audio.dialog
 * call me : jplayer_top@163.com
 * github : https://github.com/oblivion0001
 */

public class CurrentRecordDialog extends BaseCustomDialogFragment {
    @BindView(R.id.tvCurDay)
    TextView mTvCurDay;
    @BindView(R.id.tvStartTime)
    TextView mTvStartTime;
    @BindView(R.id.tvEndTime)
    TextView mTvEndTime;
    @BindView(R.id.tvSleepTime)
    TextView mTvSleepTime;
    @BindView(R.id.tvSnoringCount)
    TextView mTvSnoringCount;
    @BindView(R.id.tvRecord)
    TextView mTvRecord;

    @Override
    protected void initView(View view) {
        ButterKnife.bind(this, view);
        Bundle bundle = getArguments();
        if (bundle != null) {
            RecordSleepBean recordBean = bundle.getParcelable("record");
            if (recordBean != null) {
                mTvCurDay.setText(recordBean.day);
                mTvSnoringCount.setText(recordBean.sleepSnoring);
                mTvEndTime.setText(recordBean.endTime);
                mTvStartTime.setText(recordBean.startTime);
                mTvSleepTime.setText(recordBean.sleepTime);
            }
        }
    }

    @Override
    public int setGravity() {
        return Gravity.CENTER;
    }

    @Override
    public int setAnim() {
        return R.style.AnimCenter;
    }

    @Override
    public int setHeight() {
        return ScreenUtils.getScreenHeight() - SizeUtils.dp2px(80);
    }

    @Override
    public int initLayout() {
        return R.layout.dialog_current_record;
    }


}
