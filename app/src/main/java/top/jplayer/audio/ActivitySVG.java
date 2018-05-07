package top.jplayer.audio;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.jaredrummler.android.widget.AnimatedSvgView;
import com.vondear.rxtools.RxActivityTool;
import com.vondear.rxtools.RxBarTool;
import com.vondear.rxtools.activity.ActivityBase;

import butterknife.BindView;
import butterknife.ButterKnife;
import top.jplayer.audio.bean.ModelSVG;

/**
 * @author vondear
 */
public class ActivitySVG extends ActivityBase {

    @BindView(R.id.animated_svg_view)
    AnimatedSvgView mSvgView;
    @BindView(R.id.activity_svg)
    RelativeLayout mActivitySvg;
    @BindView(R.id.app_name)
    ImageView mAppName;
    @SuppressLint("HandlerLeak")
    private Handler checkhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            mAppName.setVisibility(View.VISIBLE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBarTool.hideStatusBar(this);
        setContentView(R.layout.activity_svg);
        ButterKnife.bind(this);
        setSvg(ModelSVG.values()[0]);
        CheckUpdate();
    }

    private void setSvg(ModelSVG modelSvg) {
        mSvgView.setGlyphStrings(modelSvg.glyphs);
        mSvgView.setFillColors(modelSvg.colors);
        mSvgView.setViewportSize(modelSvg.width, modelSvg.height);
        mSvgView.setTraceResidueColor(0x32000000);
        mSvgView.setTraceColors(modelSvg.colors);
        mSvgView.rebuildGlyphData();
        mSvgView.start();
    }


    private void CheckUpdate() {
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(500);
                    Message msg = checkhandler.obtainMessage();
                    checkhandler.sendMessage(msg);
                    Thread.sleep(2000);
                    toMain();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void toMain() {
        RxActivityTool.skipActivityAndFinish(this, ActivityLoginAct.class);
    }
}
