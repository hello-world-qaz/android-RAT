package ahmyth.mine.king.ahmyth;

import android.media.MediaFormat;

import java.util.Objects;

/**
 * Created by Administrator on 2018/1/14.
 */

public class AudioEncodeConfig {
    final String codecName;
    final String mimeType;
    final int bitRate;
    final int sampleRate;
    final int channelCount;
    final int profile;

    public AudioEncodeConfig(String codecName, String mimeType,
                             int bitRate, int sampleRate, int channelCount, int profile) {
        this.codecName = codecName;
        this.mimeType = Objects.requireNonNull(mimeType);
        this.bitRate = bitRate;
        this.sampleRate = sampleRate;
        this.channelCount = channelCount;
        this.profile = profile;
    }

    MediaFormat toFormat() {
        MediaFormat format = MediaFormat.createAudioFormat(mimeType, sampleRate, channelCount);
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, profile);
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        //format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 4096 * 4);
        return format;
    }

}
