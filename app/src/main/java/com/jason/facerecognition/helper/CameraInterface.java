package com.jason.facerecognition.helper;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;

import com.jason.facerecognition.util.FaceUtil;

import java.io.IOException;
import java.lang.reflect.Method;

import static android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;

public class CameraInterface{
	private int id = 1;
	private int Facing = -1;
	private Camera camera;
	private Context context;
	private Camera.CameraInfo cameraInfo=new Camera.CameraInfo();
	private Camera.Face face;

	public CameraInterface(Context c){
		context = c;
	}

	/*********************************************/
	public void openNextCamera(){
		id++;
		camera.release();
		camera = null;
		camera = Camera.open(id%Camera.getNumberOfCameras());
		Facing = getFacing(id%Camera.getNumberOfCameras());

	}

	public Camera startCamera(){
		try {
			if(Camera.getNumberOfCameras()>1){
				camera = Camera.open(1);
				Facing = getFacing(1);
				id++;
			}else{
				camera = Camera.open(0);
				Facing = getFacing(0);
				id++;
			}
		}catch (RuntimeException e){
			Toast.makeText(context, "打开相机失败，请解除占用！", Toast.LENGTH_LONG).show();
		}
		return camera;
	}

	public void stopCamera(){
		if(null != camera){
			camera.release();
			camera = null;
		}
	}

	/*****************************************************/

	public boolean isFacingFront(){
		return Facing==CAMERA_FACING_FRONT;
	}

	public Camera getCamera(){
		return camera;
	}

	public Camera.Face getFace(){
		return face;
	}

	public void startFaceDetection(){
		try {
			camera.startFaceDetection();
		}catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	public void stopFaceDetection(){
		camera.stopFaceDetection();
	}

	public void setFaceDetection(final FaceView faceView){
		if(faceView != null){
			faceView.clearFaces();
			faceView.setVisibility(View.VISIBLE);
		}
		camera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
			@Override
			public void onFaceDetection(Camera.Face[] faces, Camera camera1) {
				if(faces.length>0) {
					faceView.setFaces(faces, isFacingFront());
					face = faces[0];
					face.rect = FaceUtil.resize(face.rect,1.2f);
				}else{
					face = null;
				}
			}
		});
	}

	public void setFaceDetection(){
		camera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
			@Override
			public void onFaceDetection(Camera.Face[] faces, Camera camera1) {
				try {
					for(Camera.Face n:faces){
						if(n.score>face.score)face = n;
					}
				}catch (NullPointerException e){
				}
			}
		});
	}

	public boolean setCameraPreview(SurfaceHolder surfaceHolder){
		try {
			camera.setPreviewDisplay(surfaceHolder);
		}catch (IOException e) {
			Toast.makeText(context, "设置相机预览失败", Toast.LENGTH_LONG).show();
			return false;
		}catch (RuntimeException e){
			e.printStackTrace();
		}
		return true;
	}

	public boolean startCameraPreview(){
		try {
			Camera.Parameters parameters = camera.getParameters();
			parameters.setPictureFormat(PixelFormat.JPEG);
			//parameters.setPictureSize(surfaceView.getWidth(), surfaceView.getHeight());  // 部分定制手机，无法正常识别该方法。
			parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//1连续对焦
			setDispaly(parameters, camera);
			camera.setParameters(parameters);
			camera.startPreview();
			camera.cancelAutoFocus();//只有加上了这一句，才会自动对焦。
		}catch (RuntimeException e){
			Toast.makeText(context, "该手机不支持参数设定", Toast.LENGTH_LONG).show();
		}
		return true;
	}

	public void stopCameraPreview(){
		try {
			camera.stopPreview();
		}catch (RuntimeException e){
			Toast.makeText(context, "预览已关闭", Toast.LENGTH_LONG).show();
		}
	}

	public void takePicture( Camera.PictureCallback jpeg){
		try{
			camera.takePicture(null, null, jpeg);
		}catch (RuntimeException e){
			Toast.makeText(context, "请等待", Toast.LENGTH_LONG).show();
		}
	}

	//控制图像的正确显示方向
	private void setDispaly(Camera.Parameters parameters,Camera camera)
	{
		if (Integer.parseInt(Build.VERSION.SDK) >= 8){
			setDisplayOrientation(camera,90);
		}
		else{
			parameters.setRotation(90);
		}
	}

	//实现的图像的正确显示
	private void setDisplayOrientation(Camera camera, int i) {
		Method downPolymorphic;
		try{
			downPolymorphic=camera.getClass().getMethod("setDisplayOrientation", new Class[]{int.class});
			if(downPolymorphic!=null) {
				downPolymorphic.invoke(camera, new Object[]{i});
			}
		}
		catch(Exception e){
			Log.e("Came_e", "图像出错");
		}
	}

	private int getFacing(int CameraId){
		Camera.getCameraInfo(CameraId,cameraInfo);
		return cameraInfo.facing;
	}

}