package com.jason.facerecognition.data;

import android.os.Parcelable;
import android.util.Log;

import com.jason.facerecognition.recognier.Classifier;

import java.util.ArrayList;
import java.util.List;

public class FaceEigen {
    private static final String Tag = "FaceEigen";

    private String eigen;

    public FaceEigen(){
        eigen = null;
    }

    //set the List Recognition into the mfaces
    public FaceEigen(List<Classifier.Recognition> in){
        int count=0;
        StringBuilder buffer = new StringBuilder();
        for(Classifier.Recognition n:in){
            buffer.append(String.valueOf(n.getConfidence()));
            buffer.append(",");
        }
        eigen = buffer.toString();
    }

    public FaceEigen(ArrayList<Float> in){
        int count=0;
        StringBuilder buffer = new StringBuilder();
        for(Float n:in){
            buffer.append(String.valueOf(n));
            buffer.append(",");
        }
        eigen = buffer.toString();
    }

    public ArrayList<Float> getArraylist(){
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



}