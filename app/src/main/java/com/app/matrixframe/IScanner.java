package com.app.matrixframe;

import android.graphics.PointF;
import android.net.Uri;

import org.opencv.core.Mat;

import java.util.Map;

/**
 * Created by jhansi on 04/04/15.
 */
public interface IScanner {

    void onBitmapSelect(Uri uri);

    void onScanFinish(Map<Integer, PointF> points, Mat tmp);
   void onImageCutFinish(Uri uri);
}
