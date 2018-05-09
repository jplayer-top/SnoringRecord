package top.jplayer.audio;

import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.vondear.rxtools.RxTool;

/**
 * Created by Administrator on 2018/5/5.
 * rul
 */

public class AudioApplication extends MultiDexApplication {

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
        Bugly.init(this, "4b3faed579", false);
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // you must install multiDex whatever tinker is installed!
        MultiDex.install(base);


        // 安装tinker
        Beta.installTinker();
    }
}
