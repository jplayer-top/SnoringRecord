package top.jplayer.audio.dialog;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vondear.rxtools.RxKeyboardTool;
import com.vondear.rxtools.view.dialog.RxDialogShapeLoading;

import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import top.jplayer.audio.R;
import top.jplayer.audio.bean.LoginBean;
import top.jplayer.audio.utils.LoginDaoUtil;

/**
 * Created by Administrator on 2018/5/5.
 */

public class ForgetSureDialog extends BaseCustomDialogFragment {
    @BindView(R.id.et_password)
    EditText etPassword;
    @BindView(R.id.clean_password)
    ImageView cleanPassword;
    @BindView(R.id.iv_show_pwd)
    ImageView ivShowPwd;
    @BindView(R.id.textView12)
    TextView textView12;
    @BindView(R.id.tv_sure)
    TextView tvSure;
    @BindView(R.id.textView10)
    TextView textView10;
    @BindView(R.id.tv_cancle)
    TextView tvCancle;
    Unbinder unbinder;

    @Override
    protected void initView(View view) {
        unbinder = ButterKnife.bind(this, view);
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s) && cleanPassword.getVisibility() == View.GONE) {
                    cleanPassword.setVisibility(View.VISIBLE);
                } else if (TextUtils.isEmpty(s)) {
                    cleanPassword.setVisibility(View.GONE);
                }
                if (s.toString().isEmpty())
                    return;
                if (!s.toString().matches("[A-Za-z0-9]+")) {
                    String temp = s.toString();
                    Toast.makeText(getContext(), "请输入数字或字母", Toast.LENGTH_SHORT).show();
                    s.delete(temp.length() - 1, temp.length());
                    etPassword.setSelection(s.length());
                }
            }
        });
    }

    @Override
    public int initLayout() {
        return R.layout.dialog_forget_sure;
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
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    DismissListener listener;
    private RxDialogShapeLoading rxDialogShapeLoading;

    public void setOnDismisslistener(DismissListener listener) {
        this.listener = listener;
    }

    public interface DismissListener {
        void onDismiss();
    }

    @OnClick({R.id.clean_password, R.id.iv_show_pwd, R.id.tv_sure, R.id.tv_cancle})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.clean_password:
                etPassword.setText("");
                break;
            case R.id.iv_show_pwd:
                if (etPassword.getInputType() != InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    ivShowPwd.setImageResource(R.drawable.pass_visuable);
                } else {
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    ivShowPwd.setImageResource(R.drawable.pass_gone);
                }
                String pwd = etPassword.getText().toString();
                if (!TextUtils.isEmpty(pwd))
                    etPassword.setSelection(pwd.length());
                break;
            case R.id.tv_sure:
                Bundle bundle = getArguments();
                assert bundle != null;
                String phone = bundle.getString("phone");
                String password = etPassword.getText().toString();
                RxKeyboardTool.hideSoftInput(getActivity());
                LoginDaoUtil loginDaoUtil = new LoginDaoUtil(getContext());
                if (TextUtils.equals(password, "")) {
                    Toast.makeText(getContext(), "请输入要修改的密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<LoginBean> beans = loginDaoUtil.queryAllloginBean();
                LoginBean realBean = null;
                for (LoginBean bean : beans) {
                    if (bean.name.equals(phone)) {
                        realBean = bean;
                    }
                }
                if (realBean != null) {
                    realBean.password = password;
                    loginDaoUtil.updateloginBean(realBean);
                    rxDialogShapeLoading = new RxDialogShapeLoading(getContext());
                    rxDialogShapeLoading.show();
                    dismiss();
                    int delayMillis = (new Random().nextInt(3) + 1) * 1000;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            rxDialogShapeLoading.dismiss();
                            if (listener != null) {
                                listener.onDismiss();
                            }
                        }
                    }, delayMillis);
                }
                break;
            case R.id.tv_cancle:
                dismiss();
                break;
        }
    }
}
