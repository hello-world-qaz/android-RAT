package ahmyth.mine.king.ahmyth;
import android.app.Activity;
/**
 * Created by Administrator on 2018/1/6.
 */

public interface IActivityManager {
    <T extends Activity> void addActivity(T t);

    <T extends Activity> void removeActivity(T t);

    void removeAcitivtyByClazz(Class<? extends Activity> clazz);
}
