package ahmyth.mine.king.ahmyth;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.content.Intent;

/**
 * Created by Administrator on 2018/1/6.
 */

public class PointActivity extends BaseActivity {
    public static PointActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.point);
        instance = this;
        //Log.d("creat","instance:"+instance);
        Window window = getWindow();
        window.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 0;
        params.height = 10;
        params.width = 10;
        window.setAttributes(params);
        Log.d("Ahmyth","onCreate: " +instance);
    }
    /**
     * 开启保活页面
     */
    public static void startpointActivity() {
        Intent intent = new Intent("pointactivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Myapplication.getAppContext().startActivity(intent);
        Log.d("Ahmyth","Startpointactivity: ");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    /**
     * 关闭保活页面
     */
    public static void killpointActivity() {
        if(instance != null){
            instance.finish();
            Log.d("Ahmyth","killpointActivity");
        }
    }
}
