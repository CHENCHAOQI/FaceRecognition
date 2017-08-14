package com.jason.facerecognition.data;

import android.app.Application;

import com.jason.facerecognition.recognier.TensorflowLoader;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/8/9.
 */

public class Global extends Application {
    public static boolean user;
    public static TensorflowLoader loader;

    private ArrayList<String> globalFaces = new ArrayList<>();

    public ArrayList<String> getGlobalVariable() {
        return globalFaces;
    }

    public void setGlobalVariable(String face) {
        this.globalFaces.add(face);
    }

    public void clear(){
        this.globalFaces.clear();
    }

}
