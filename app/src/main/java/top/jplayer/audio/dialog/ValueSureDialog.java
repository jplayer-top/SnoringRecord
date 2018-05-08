package top.jplayer.audio.dialog;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import top.jplayer.audio.R;

/**
 * Created by Administrator on 2018/5/5.
 * 输入法阀值
 */

public class ValueSureDialog extends BaseCustomDialogFragment {

    @BindView(R.id.tv_content)
    public EditText mTvContent;
    @BindView(R.id.tv_sure)
    TextView mTvSure;
    @BindView(R.id.tv_cancel)
    TextView mTvCancel;
    public int value = 60;
    SureListener listener;

    public interface SureListener {
        void onSureListener(int value);
    }

    public void setSureListener(SureListener listener) {
        this.listener = listener;
    }

    @Override
    protected void initView(View view) {
        ButterKnife.bind(this, view);

    }

    @Override
    public int initLayout() {
        return R.layout.dialog_value_sure;
    }


    @OnClick({R.id.tv_sure, R.id.tv_cancel})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_sure:
                value = Integer.parseInt(mTvContent.getText().toString());
                listener.onSureListener(value);
                dismiss();
                break;
            case R.id.tv_cancel:
                dismiss();
                break;
        }
    }
}
