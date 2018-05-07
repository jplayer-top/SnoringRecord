package top.jplayer.audio;

import android.app.Application;
import android.os.Build;
import android.os.StrictMode;

import com.vondear.rxtools.RxTool;

/**
 * Created by Administrator on 2018/5/5.
 * rul
 */

public class AudioApplication extends Application {

    public static AudioApplication application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        RxTool.init(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
    }
}
