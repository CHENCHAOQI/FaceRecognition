package com.jason.facerecognition.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.content.Intent;
import android.hardware.Camera.PictureCallback;
import android.widget.Toast;

import com.jason.facerecognition.helper.FaceView;
import com.jason.facerecognition.helper.CameraInterface;
import com.jason.facerecognition.data.FaceEigen;
import com.jason.facerecognition.data.Global;
import com.jason.facerecognition.recognier.TensorflowLoader;
import com.jason.facerecognition.util.FaceUtil;
import com.jason.facerecognition.R;
import com.jason.facerecognition.util.ImageUtil;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final String Tag = "MainActivity";

    private Button btnGetInto, btnSignup;
    private ImageButton btnGetFace,btnToggleCamera;
    private ImageView face1Result;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private FaceView faceView;

    private PictureCallback pictureCallback;
    private Global mGlobal;
    private TensorflowLoader loader;
    private Executor executor;
    private CameraInterface mInterface;

    private static final String info = "这里是识别界面，您还未录入人脸数据";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();//初始化
        if(!Global.user)Toast.makeText(MainActivity.this, info , Toast.LENGTH_LONG).show();

        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.setKeepScreenOn(true);
        surfaceView.setFocusable(true);
        surfaceView.setBackgroundColor(TRIM_MEMORY_BACKGROUND);
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // TODO Auto-generated method stub
                mInterface.stopCamera();
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                // TODO Auto-generated method stub
                mInterface.startCamera();
                mInterface.setCameraPreview(surfaceHolder);
                mInterface.startCameraPreview();
                mInterface.startFaceDetection();
                mInterface.setFaceDetection(faceView);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                //实现自动对焦
                //try为暂时对策
                try{
                    mInterface.getCamera().autoFocus(new Camera.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(boolean success, Camera camera) {
                            if (success) {
                                mInterface.startCameraPreview();//实现相机的参数初始化
                                mInterface.getCamera().cancelAutoFocus();//只有加上了这一句，才会自动对焦。
                            }
                        }
                    });
                }catch (RuntimeException e){
                    e.printStackTrace();
                }
            }
        });

        pictureCallback = new PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                // TODO Auto-generated method stub
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                //获取人脸
                if(null==mInterface.getFace()) {
                    face1Result.setVisibility(View.INVISIBLE);
                    Toast.makeText(MainActivity.this, "获取人脸失败", Toast.LENGTH_LONG).show();
                }
                else{
                    bitmap = FaceUtil.getFace(bitmap,mInterface.getFace());
                    if(mInterface.isFacingFront())bitmap = ImageUtil.getRotateBitmap(bitmap,-90);
                    else bitmap  = ImageUtil.getRotateBitmap(bitmap,90);
                    //识别人脸
                    if(null!=bitmap) {
                        ArrayList<String> userFace = mGlobal.getGlobalVariable();
                        FaceEigen thisFace = new FaceEigen(loader.recognize(bitmap));
                        String s="";
                        boolean success=false;
                        double confidence;
                        for(String n:userFace){
                            FaceEigen thatFace = new FaceEigen(FaceUtil.getArraylist(n)) ;
                            confidence = FaceUtil.compareFace(thisFace, thatFace);
                            if(confidence<0.68) {
                                success=true;
                                makeEntranceVisible();
                            }
                            s = s + "自信度：" + FaceUtil.getConfidence(confidence) +"\n";
                        }
                        if(success)s="认证成功\n"+ s;
                        else s="认证失败\n"+ s;
                        Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
                    }else;
                }
                mInterface.startCameraPreview();
                mInterface.startFaceDetection();
                mInterface.setFaceDetection(faceView);
            }
        };

        btnGetFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Camera.Face face = mInterface.getFace();
                mGlobal = (Global) getApplicationContext();
                if(!mGlobal.user){
                    Toast.makeText(MainActivity.this, "请先录入用户", Toast.LENGTH_LONG).show();
                }
                else if(face==null){
                    Toast.makeText(MainActivity.this, "未检测到人脸", Toast.LENGTH_LONG).show();
                }else{
                    mInterface.takePicture(pictureCallback);
                }
            }
        });

        btnToggleCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInterface.openNextCamera();
                mInterface.setCameraPreview(surfaceHolder);
                mInterface.startCameraPreview();
                mInterface.startFaceDetection();
                mInterface.setFaceDetection(faceView);
            }
        });

        btnGetInto.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                startActivity(intent);
                //MainActivity.this.finish();
            }
        });

        btnSignup.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Main3Activity.class);
                startActivity(intent);
                //MainActivity.this.finish();
            }
        });

    }

    private void initView(){

        face1Result = (ImageView) findViewById(R.id.face1);
        btnToggleCamera = (ImageButton) findViewById(R.id.btnToggleCamera);
        btnGetFace = (ImageButton) findViewById(R.id.btnGetFace);
        btnGetInto = (Button) findViewById(R.id.btnGetInto);
        btnSignup =(Button) findViewById(R.id.btnsignup);
        surfaceView =  (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        mGlobal = (Global) getApplicationContext();
        mInterface = new CameraInterface(MainActivity.this);
        executor = Executors.newSingleThreadExecutor();
        loader = Global.loader;
        faceView = (FaceView) findViewById(R.id.face_view);
        faceView = (FaceView) findViewById(R.id.face_view);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mGlobal = ((Global) getApplicationContext());

        mInterface.startCamera();
        mInterface.setCameraPreview(surfaceHolder);
        mInterface.startCameraPreview();
        mInterface.setFaceDetection(faceView);
        mInterface.startFaceDetection();
    }

    @Override
    protected void onPause() {
        //cameraView.stop();
        super.onPause();
        mInterface.stopCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void makeButtonVisible() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnGetFace.setVisibility(View.VISIBLE);
            }
        });
    }

    private void makeEntranceVisible() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnGetInto.setVisibility(View.VISIBLE);
            }
        });
    }

    private void makeEntranceInvisible(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnGetInto.setVisibility(View.INVISIBLE);
            }
        });
    }
}
