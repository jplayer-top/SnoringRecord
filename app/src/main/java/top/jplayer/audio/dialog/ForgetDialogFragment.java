package top.jplayer.audio.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.vondear.rxtools.RxBarTool;
import com.vondear.rxtools.RxKeyboardTool;
import com.vondear.rxtools.view.dialog.RxDialogShapeLoading;

import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import top.jplayer.audio.MainActivity;
import top.jplayer.audio.R;
import top.jplayer.audio.bean.LoginBean;
import top.jplayer.audio.utils.LoginDaoUtil;
import top.jplayer.audio.utils.ScreenUtils;

/**
 * Created by Administrator on 2018/5/5.
 * 注册
 */

public class ForgetDialogFragment extends BaseCustomDialogFragment {
    @BindView(R.id.et_mobile)
    EditText etMobile;
    @BindView(R.id.iv_clean_phone)
    ImageView ivCleanPhone;
    @BindView(R.id.et_password)
    EditText etPassword;
    @BindView(R.id.clean_password)
    ImageView cleanPassword;
    @BindView(R.id.iv_show_pwd)
    ImageView ivShowPwd;
    @BindView(R.id.btn_login)
    Button btnLogin;
    Unbinder unbinder;
    @BindView(R.id.content)
    LinearLayout content;
    @BindView(R.id.scrollView)
    ScrollView scrollView;
    @BindView(R.id.root)
    RelativeLayout root;
    Unbinder unbinder1;
    private RxDialogShapeLoading rxDialogShapeLoading;

    @Override
    protected void initView(View view) {
        unbinder = ButterKnife.bind(this, view);
        RxBarTool.setTransparentStatusBar(getActivity());//状态栏透明化
        RxBarTool.StatusBarLightMode(getActivity());
        btnLogin.setText("找回密码");
        initEvent();
    }

    private void initEvent() {
        etMobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s) && ivCleanPhone.getVisibility() == View.GONE) {
                    ivCleanPhone.setVisibility(View.VISIBLE);
                } else if (TextUtils.isEmpty(s)) {
                    ivCleanPhone.setVisibility(View.GONE);
                }
            }
        });
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
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RxKeyboardTool.hideSoftInput(getActivity());
                LoginDaoUtil loginDaoUtil = new LoginDaoUtil(getContext());
                final String phone = etMobile.getText().toString();
                String password = etPassword.getText().toString();
                if (TextUtils.equals(phone, "")) {
                    Toast.makeText(getContext(), "请输入用户名", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.equals(password, "")) {
                    Toast.makeText(getContext(), "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<LoginBean> beans = loginDaoUtil.queryAllloginBean();
                LoginBean realBean = null;
                for (LoginBean bean : beans) {
                    if (bean.name.equals(phone) && bean.password.equals(password)) {
                        realBean = bean;
                    }
                }
                rxDialogShapeLoading = new RxDialogShapeLoading(getContext());
                rxDialogShapeLoading.show();
                int delayMillis = (new Random().nextInt(3) + 1) * 1000;
                Handler handler = new Handler();
                if (realBean != null) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            rxDialogShapeLoading.dismiss();
                            ForgetSureDialog dialog = new ForgetSureDialog();
                            Bundle bundle = new Bundle();
                            bundle.putString("phone", phone);
                            dialog.setArguments(bundle);
                            dialog.setOnDismisslistener(new ForgetSureDialog.DismissListener() {
                                @Override
                                public void onDismiss() {
                                    dismiss();
                                }
                            });
                            dialog.show(getActivity().getSupportFragmentManager(), "forget_sure");
                        }
                    }, delayMillis);
                } else {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            rxDialogShapeLoading.dismiss();
                            Toast.makeText(getContext(), "账号密码不正确，请重新输入", Toast.LENGTH_SHORT).show();
                        }
                    }, delayMillis);
                }
            }
        });
    }

    @Override
    public int setHeight() {
        return ScreenUtils.getScreenHeight();
    }

    @Override
    public int setAnim() {
        return R.style.AnimBottom;
    }

    @Override
    public int setGravity() {
        return Gravity.BOTTOM;
    }

    @Override
    public int initLayout() {
        return R.layout.dialog_register;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.iv_clean_phone, R.id.clean_password, R.id.iv_show_pwd})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_clean_phone:
                etMobile.setText("");
                break;
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
        }
    }
}
