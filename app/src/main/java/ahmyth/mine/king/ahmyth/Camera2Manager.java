package ahmyth.mine.king.ahmyth;

/**
 * Created by Administrator on 2018/1/7.
 */
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraAccessException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class Camera2Manager {
    private Context context;
    private CameraManager cameraManager;
    public Camera2Manager(Context context) {
        this.context = context;
    }






    public void Startup(String mCameraID){
        CameraActivity.startCameraActivity(mCameraID);
        //ConnectionManager.delay(5000);


        //initView(mCameraID);
    }



    public JSONObject findCameraList() {

        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return null;
        }
        try {
            JSONObject cameras = new JSONObject();
            JSONArray list = new JSONArray();
            cameras.put("camList", true);
            cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics characteristics;
            try {
                for(String cameraID:cameraManager.getCameraIdList()){
                    characteristics = cameraManager.getCameraCharacteristics(cameraID);
                    Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                    if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                        JSONObject jo = new JSONObject();
                        jo.put("name", "Front");
                        jo.put("id", cameraID);
                        list.put(jo);
                    }else if(facing != null && facing == CameraCharacteristics.LENS_FACING_BACK){
                        JSONObject jo = new JSONObject();
                        jo.put("name", "Back");
                        jo.put("id", cameraID);
                        list.put(jo);
                    }

                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            cameras.put("list" , list);
            return cameras;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}


