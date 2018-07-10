package ahmyth.mine.king.ahmyth;

import android.media.MediaFormat;

/**
 * Created by Administrator on 2018/1/14.
 */

class AudioEncoder extends BaseEncoder {
    private final AudioEncodeConfig mConfig;

    AudioEncoder(AudioEncodeConfig config) {
        super(config.codecName);
        this.mConfig = config;
    }

    @Override
    protected MediaFormat createMediaFormat() {
        return mConfig.toFormat();
    }

}
