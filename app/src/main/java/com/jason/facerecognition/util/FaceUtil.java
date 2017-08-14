package com.jason.facerecognition.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.Log;

import com.jason.facerecognition.data.FaceEigen;
import com.jason.facerecognition.recognier.Classifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by jason on 17-8-6.
 */

public class FaceUtil {

    public static double compareFace(FaceEigen mface1, FaceEigen mface2){
        double runSum=0;
        Iterator<Float> itFace1 = mface1.getArraylist().iterator();
        Iterator<Float> itFace2 = mface2.getArraylist().iterator();
        //get the sum of power of the error
        while(itFace1.hasNext()){
            runSum+=Math.pow(itFace1.next()-itFace2.next(),2);
        }
        return Math.sqrt(runSum);
    }

    //getface by static methods bitmap input
    public static Bitmap getFace(Bitmap bitmap){
        bitmap = bitmap.copy(Bitmap.Config.RGB_565, true);

        android.media.FaceDetector.Face[] mFace;
        android.media.FaceDetector mDector;
        int numOfMaxFaces=10;
        mFace=new android.media.FaceDetector.Face[numOfMaxFaces];
        mDector = new android.media.FaceDetector(bitmap.getWidth(),bitmap.getHeight(),numOfMaxFaces);
        int numResults = mDector.findFaces(bitmap,mFace);
        //获得人脸的数量
        if(numResults>1) {
            if (mFace[0].confidence()>0.4){
                PointF eyeMidPoint = new PointF();
                mFace[0].getMidPoint(eyeMidPoint);
                float eyesDistance = mFace[0].eyesDistance();

                bitmap = Bitmap.createBitmap(
                        bitmap,
                        (int) (eyeMidPoint.x - eyesDistance),
                        (int) (eyeMidPoint.y - eyesDistance),
                        (int) (eyesDistance * 2),
                        (int) (eyesDistance * 2.5)
                );

                return bitmap;
                }
        }
        return null;
    }

    public static Rect resize(Rect rect, float mul){
        float x = rect.exactCenterX();
        float y = rect.exactCenterY();
        float toBT = Math.abs(rect.exactCenterY()-rect.bottom)*mul;
        float toLR = Math.abs(rect.exactCenterX()-rect.left)*mul;

        Rect resized = new Rect(
                Math.round(x-toLR),
                Math.round(y-toBT),
                Math.round( x+toLR),
                Math.round(y+toBT)
        );

        return resized;
    }

    //getface use the rectangular
    public static Bitmap getFace(Bitmap bitmap, Camera.Face face){
        RectF rectF =  FaceUtil.transform(face.rect,bitmap.getWidth(),bitmap.getHeight());
        //Log.i("s",rectF.left+" "+rectF.top+" "+rectF.right+" "+rectF.bottom+" "+(rectF.right-rectF.left)+" "+(rectF.bottom-rectF.top)+" "+bitmap.getHeight()+" "+bitmap.getWidth());
        rectF = checkInBound(rectF,bitmap.getWidth(),bitmap.getHeight());
        bitmap = Bitmap.createBitmap(
                    bitmap,
                    Math.round(rectF.left),
                    Math.round(rectF.top),
                    Math.round(rectF.right)-Math.round(rectF.left),
                    Math.round(rectF.bottom)-Math.round(rectF.top)
        );
        return bitmap;
    }

    public static String toString(List<Classifier.Recognition> in){
        int count=0;
        StringBuilder buffer = new StringBuilder();
        for(Classifier.Recognition n:in){
            buffer.append(String.valueOf(n.getConfidence()));
            buffer.append(",");
        }
        return buffer.toString();
    }

    public static ArrayList<Float> getArraylist(String eigen){
        StringBuilder buffer = new StringBuilder(eigen);
        ArrayList<Float> eigenList = new ArrayList<>();
        String thisFloat;
        while(buffer.length()!=1) {
            thisFloat = buffer.substring(0, buffer.indexOf(","));
            //切割buffer
            if(buffer.indexOf(",")!=buffer.length()-1)buffer.delete(0,buffer.indexOf(",")+1);
            else buffer.delete(0,buffer.indexOf(","));
            //插入到ArrayList
            eigenList.add(Float.parseFloat(thisFloat));
        }
        return eigenList;
    }

    public static int getConfidence(double x){
        if(x<0.3)return 100;
        else if(x<0.68)return (int)(-52.63*x+115.79);
        else if(x<1.5)return (int)(-97.56*x+146.34);
        else return 0;
    }

    private static RectF transform(Rect rect, int width, int height){
        Matrix mMatrix = new Matrix();
        RectF mRect = new RectF();
        boolean isMirror = false;
        //int Id = CameraInterface.getInstance().getCameraId();
        //if(Id == Camera.CameraInfo.CAMERA_FACING_BACK)isMirror = false;
        //else if(Id == Camera.CameraInfo.CAMERA_FACING_FRONT)isMirror = true;
        Util.prepareMatrix(mMatrix, isMirror, 0, width, height);
        mRect.set(rect);
        mMatrix.mapRect(mRect);

        return mRect;
    }

    private static RectF checkInBound(RectF rect, int width , int height){
        if(rect.left<0)rect.left=0;
        if(rect.top<0)rect.top=0;
        if(rect.bottom>height)rect.bottom=(float) height;
        if(rect.right>width)rect.right=(float) width;
        //Log.i("checker",rect.left+""+rect.top+""+rect.right+""+rect.bottom+"");
        return rect;
    }

}

