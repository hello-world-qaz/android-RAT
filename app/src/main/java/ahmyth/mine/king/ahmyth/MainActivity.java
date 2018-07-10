package ahmyth.mine.king.ahmyth;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import ahmyth.mine.king.ahmyth.permission.FloatWindowManager;

public class MainActivity extends Activity {
    public static MainActivity MainActivityInstance;
    private volatile FloatWindowManager instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MainActivityInstance = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("Ahmyth","start mainactivity");

        instance = FloatWindowManager.getInstance();
        if(!instance.checkPermission(MainActivity.this)){
            instance.applyPermission(MainActivity.this);
        }

    }
}
