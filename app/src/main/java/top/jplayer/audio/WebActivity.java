package top.jplayer.audio;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.method.MovementMethod;
import android.util.Log;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vondear.rxtools.RxBarTool;
import com.vondear.rxtools.RxImageTool;
import com.vondear.rxtools.RxKeyboardTool;
import com.vondear.rxtools.activity.ActivityBase;
import com.vondear.rxtools.activity.ActivityWebView;
import com.vondear.rxtools.view.RxTextAutoZoom;

/**
 * Created by Administrator on 2018/5/5.
 * webView
 */

public class WebActivity extends ActivityBase {
    private static final int TIME_INTERVAL = 2000;
    ProgressBar pbWebBase;
    TextView tvTitle;
    WebView webBase;
    ImageView ivFinish;
    RxTextAutoZoom mRxTextAutoZoom;
    LinearLayout llIncludeTitle;
    private String webPath = "";
    private long mBackPressed;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBarTool.setTransparentStatusBar(this);
        this.setContentView(com.vondear.rxtools.R.layout.activity_webview);
        this.getWindow().setSoftInputMode(32);
        this.initView();
        this.initData();
    }

    private void initView() {
        this.mRxTextAutoZoom = (RxTextAutoZoom) this.findViewById(com.vondear.rxtools.R.id.afet_tv_title);
        this.llIncludeTitle = (LinearLayout) this.findViewById(com.vondear.rxtools.R.id.ll_include_title);
        this.tvTitle = (TextView) this.findViewById(com.vondear.rxtools.R.id.tv_title);
        this.pbWebBase = (ProgressBar) this.findViewById(com.vondear.rxtools.R.id.pb_web_base);
        this.webBase = (WebView) this.findViewById(com.vondear.rxtools.R.id.web_base);
        this.ivFinish = (ImageView) this.findViewById(com.vondear.rxtools.R.id.iv_finish);
        this.ivFinish.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (WebActivity.this.webBase.canGoBack()) {
                    WebActivity.this.webBase.goBack();
                } else {
                    WebActivity.this.finish();
                }

            }
        });
        this.initAutoFitEditText();
    }

    public void initAutoFitEditText() {
        this.mRxTextAutoZoom.clearFocus();
        this.mRxTextAutoZoom.setEnabled(false);
        this.mRxTextAutoZoom.setFocusableInTouchMode(false);
        this.mRxTextAutoZoom.setFocusable(false);
        this.mRxTextAutoZoom.setEnableSizeCache(false);
        this.mRxTextAutoZoom.setMovementMethod((MovementMethod) null);
        this.mRxTextAutoZoom.setMaxHeight(RxImageTool.dip2px(55.0F));
        this.mRxTextAutoZoom.setMinTextSize(Float.valueOf(37.0F));
        RxTextAutoZoom.setNormalization(this, this.llIncludeTitle, this.mRxTextAutoZoom);
        RxKeyboardTool.hideSoftInput(this);
    }

    private void initData() {
        this.pbWebBase.setMax(100);
        Bundle bundle = getIntent().getBundleExtra("bundle");
        this.webPath = bundle.getString("url");
        if ("".equals(this.webPath)) {
            this.webPath = "http://www.baidu.com";
        }

        WebSettings webSettings = this.webBase.getSettings();
        if (Build.VERSION.SDK_INT >= 19) {
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }

        if (Build.VERSION.SDK_INT >= 19) {
            webSettings.setLoadsImagesAutomatically(true);
        } else {
            webSettings.setLoadsImagesAutomatically(false);
        }

        if (Build.VERSION.SDK_INT >= 11) {
            this.webBase.setLayerType(1, (Paint) null);
        }

        this.webBase.setLayerType(2, (Paint) null);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setSavePassword(true);
        webSettings.setDomStorageEnabled(true);
        this.webBase.setSaveEnabled(true);
        this.webBase.setKeepScreenOn(true);
        this.webBase.setWebChromeClient(new WebChromeClient() {
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                WebActivity.this.mRxTextAutoZoom.setText(title);
            }

            public void onProgressChanged(WebView view, int newProgress) {
                WebActivity.this.pbWebBase.setProgress(newProgress);
                super.onProgressChanged(view, newProgress);
            }
        });
        this.webBase.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (!WebActivity.this.webBase.getSettings().getLoadsImagesAutomatically()) {
                    WebActivity.this.webBase.getSettings().setLoadsImagesAutomatically(true);
                }

                WebActivity.this.pbWebBase.setVisibility(View.GONE);
            }

            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                WebActivity.this.pbWebBase.setVisibility(View.VISIBLE);
                super.onPageStarted(view, url, favicon);
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!url.startsWith("http:") && !url.startsWith("https:")) {
                    try {
                        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
                        WebActivity.this.startActivity(intent);
                    } catch (Exception var4) {
                        var4.printStackTrace();
                    }

                    return true;
                } else {
                    view.loadUrl(url);
                    return false;
                }
            }
        });
        this.webBase.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String paramAnonymousString1, String paramAnonymousString2, String paramAnonymousString3, String paramAnonymousString4, long paramAnonymousLong) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(Uri.parse(paramAnonymousString1));
                WebActivity.this.startActivity(intent);
            }
        });
        this.webBase.loadUrl(this.webPath);
        Log.v("帮助类完整连接", this.webPath);
    }

    protected void onSaveInstanceState(Bundle paramBundle) {
        super.onSaveInstanceState(paramBundle);
        paramBundle.putString("url", this.webBase.getUrl());
    }

    public void onConfigurationChanged(Configuration newConfig) {
        try {
            super.onConfigurationChanged(newConfig);
            if (this.getResources().getConfiguration().orientation == 2) {
                Log.v("Himi", "onConfigurationChanged_ORIENTATION_LANDSCAPE");
            } else if (this.getResources().getConfiguration().orientation == 1) {
                Log.v("Himi", "onConfigurationChanged_ORIENTATION_PORTRAIT");
            }
        } catch (Exception var3) {
            ;
        }

    }

    public void onBackPressed() {
        if (this.webBase.canGoBack()) {
            this.webBase.goBack();
        } else {
            if (this.mBackPressed + 2000L > System.currentTimeMillis()) {
                super.onBackPressed();
                return;
            }

            Toast.makeText(this.getBaseContext(), "再次点击返回键退出", Toast.LENGTH_SHORT).show();
            this.mBackPressed = System.currentTimeMillis();
        }

    }
}