package ahmyth.mine.king.ahmyth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Administrator on 2018/2/10.
 */

public class AutoStartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, MainService.class);
        context.startService(i);
    }
}
