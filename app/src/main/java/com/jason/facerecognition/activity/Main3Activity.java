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
import android.widget.Toast;

import com.jason.facerecognition.data.Global;
import com.jason.facerecognition.helper.CameraInterface;
import com.jason.facerecognition.helper.FaceView;
import com.jason.facerecognition.recognier.TensorflowLoader;
import com.jason.facerecognition.util.FaceUtil;
import com.jason.facerecognition.util.ImageUtil;
import com.jason.facerecognition.R;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class Main3Activity extends Activity {
    private static String Tag = "MainActivity3";

    private Button btnDelete;
    private ImageButton btnToggleCamera, btnSignup;
    private ImageView face1Result;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private FaceView faceView;

    Camera.PictureCallback pictureCallback;
    private Global mGlobal;
    private TensorflowLoader loader;
    private Executor executor;
    private CameraInterface mInterface;

    private static final String info =
            "这里是登入界面，请拍几张自己的照片\n拍完后返回识别界面";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        initView();//初始化
        if(!Global.user){
            Toast.makeText(Main3Activity.this,info, Toast.LENGTH_LONG).show();
        }

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

        pictureCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                // TODO Auto-generated method stub
                //解析data为bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                //获取人脸
                Camera.Face face = mInterface.getFace();
                if(null==face) {
                    face1Result.setVisibility(View.INVISIBLE);
                    Toast.makeText(Main3Activity.this, "获取人脸失败", Toast.LENGTH_LONG).show();
                }
                else {
                    //根据Rect face框出人脸
                    bitmap = FaceUtil.getFace(bitmap,face);
                    if(mInterface.isFacingFront())bitmap = ImageUtil.getRotateBitmap(bitmap,-90);
                    else bitmap  = ImageUtil.getRotateBitmap(bitmap,90);
                    //设置全局变量
                    mGlobal.setGlobalVariable(FaceUtil.toString(loader.recognize(bitmap)));
                    mGlobal.user = true;

                    face1Result.setVisibility(View.VISIBLE);
                    face1Result.setImageBitmap(bitmap);
                    Toast.makeText(Main3Activity.this, "这是第"+mGlobal.getGlobalVariable().size()+"张脸", Toast.LENGTH_LONG).show();
                }
                mInterface.startCameraPreview();
                mInterface.startFaceDetection();
                mInterface.setFaceDetection(faceView);
            }
        };

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

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInterface.takePicture(pictureCallback);
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Global mGlobal = ((Global) getApplicationContext());
                mGlobal.clear();
                mGlobal.user = false;
                Toast.makeText(Main3Activity.this, "删除完成", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initView(){
        face1Result = (ImageView) findViewById(R.id.face1);
        btnToggleCamera = (ImageButton) findViewById(R.id.btnToggleCamera);
        btnSignup = (ImageButton) findViewById(R.id.btnGetFace);
        btnDelete = (Button) findViewById(R.id.btndelete);
        surfaceView =  (SurfaceView) findViewById(R.id.surfaceView);
        faceView = (FaceView) findViewById(R.id.face_view) ;

        surfaceHolder = surfaceView.getHolder();
        mGlobal = ((Global) getApplicationContext());
        mInterface = new CameraInterface(Main3Activity.this);
        executor = Executors.newSingleThreadExecutor();
        loader = Global.loader;
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
        super.onPause();
        mInterface.stopCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
