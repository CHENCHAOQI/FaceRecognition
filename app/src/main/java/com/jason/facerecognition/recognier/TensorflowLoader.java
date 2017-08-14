package com.jason.facerecognition.recognier;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.List;

/**
 * Created by Administrator on 2017/8/8.
 */

public class TensorflowLoader {

    private static final int INPUT_SIZE = 160;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "embeddings";

    private static final String MODEL_FILE = "file:///android_asset/optimized_facenet.pb";
    private static final String LABEL_FILE = "file:///android_asset/recognition";

    private Classifier classifier;

    public void load(Context context) {
        try {
            classifier = TensorflowFaceClassifier.create(
                    context.getAssets(),
                    MODEL_FILE,
                    LABEL_FILE,
                    INPUT_SIZE,
                    IMAGE_MEAN,
                    IMAGE_STD,
                    INPUT_NAME,
                    OUTPUT_NAME);
        } catch (final Exception e) {
            throw new RuntimeException("Error initializing TensorFlow!", e);
        }
    }

    public boolean isNull(){
        return null==classifier;
    }

    public void close(){
        classifier.close();
    }

    public List<Classifier.Recognition> recognize(Bitmap bitmap){
        bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
        return classifier.recognizeImage(bitmap);
    }

}
