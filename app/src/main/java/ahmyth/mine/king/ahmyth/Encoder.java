package ahmyth.mine.king.ahmyth;

import java.io.IOException;

/**
 * Created by Administrator on 2018/1/14.
 */

interface Encoder {
    void prepare() throws IOException;

    void stop();

    void release();

    void setCallback(Callback callback);

    interface Callback {
        void onError(Encoder encoder, Exception exception);
    }
}
