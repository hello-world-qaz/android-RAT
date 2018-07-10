package ahmyth.mine.king.ahmyth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Administrator on 2018/2/27.
 */

public class ScreenReceiver extends BroadcastReceiver {
    @Override

    public void onReceive(Context context, Intent intent) {

//[1]获取到当前广播的事件类型

        String action = intent.getAction();

//[2]对当前广播事件类型做一个判断

        if ("android.intent.action.SCREEN_OFF".equals(action)) {

            Log.d("BROADCAST","屏幕锁屏了");
            PointActivity.startpointActivity();

        }else if ("android.intent.action.SCREEN_ON".equals(action)) {

            Log.d("BROADCAST","屏幕解锁了");
            PointActivity.killpointActivity();
        }

    }
}
