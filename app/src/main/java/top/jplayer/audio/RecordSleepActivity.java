package top.jplayer.audio;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.necer.ncalendar.calendar.NCalendar;
import com.necer.ncalendar.listener.OnCalendarChangedListener;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import top.jplayer.audio.bean.RecordSleepBean;
import top.jplayer.audio.dialog.CurrentRecordDialog;
import top.jplayer.audio.utils.RecordDaoUtil;

import static top.jplayer.audio.utils.DateUtils.getCurrentBigMonth;

/**
 * Created by Obl on 2018/5/7.
 * top.jplayer.audio
 * call me : jplayer_top@163.com
 * github : https://github.com/oblivion0001
 */

public class RecordSleepActivity extends AppCompatActivity {
    @BindView(R.id.ncalendarrrr)
    NCalendar mNcalendar;
    @BindView(R.id.showRecord)
    Button showRecord;
    private Unbinder mUnbinder;
    private ActionBar mActionBar;
    private List<RecordSleepBean> mRecordSleepBeans;
    RecordSleepBean recordBean;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_record);
        mActionBar = getSupportActionBar();
        mUnbinder = ButterKnife.bind(this);

        RecordDaoUtil recordDaoUtil = new RecordDaoUtil(this);
        mRecordSleepBeans = recordDaoUtil.queryAllbean();

        List<String> list = new ArrayList<>();
        for (RecordSleepBean recordSleepBean : mRecordSleepBeans) {
            list.add(recordSleepBean.day);
        }
        mNcalendar.setOnCalendarChangedListener(new OnCalendarChangedListener() {
            @Override
            public void onCalendarChanged(LocalDate date) {
                Log.e("asda", date.toString());
                if (mActionBar != null) {
                    mActionBar.setTitle(getCurrentBigMonth(date.toString()));
                }
                for (RecordSleepBean recordSleepBean : mRecordSleepBeans) {
                    if (TextUtils.equals(recordSleepBean.day, date.toString())) {
                        recordBean = recordSleepBean;
                        showRecord.setEnabled(true);
                        break;
                    } else {
                        showRecord.setEnabled(false);
                        recordBean = null;
                    }
                }
            }
        });

        showRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recordBean != null) {
                    CurrentRecordDialog dialog = new CurrentRecordDialog();
                    Bundle arguments = new Bundle();
                    arguments.putParcelable("record", recordBean);
                    dialog.setArguments(arguments);
                    dialog.show(getSupportFragmentManager(), "current");
                } else {
                    Toast.makeText(RecordSleepActivity.this, "当日无测量数据", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mNcalendar.setPoint(list);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }
}
