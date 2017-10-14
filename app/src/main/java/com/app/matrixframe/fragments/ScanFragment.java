package com.app.matrixframe.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.app.matrixframe.IScanner;
import com.app.matrixframe.R;
import com.app.matrixframe.ScanActivity;
import com.app.matrixframe.util.PolygonView;
import com.app.matrixframe.util.ScanConstants;
import com.app.matrixframe.util.SharedPreference;
import com.app.matrixframe.util.Util;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import MattressDimension.TransformImage;

/**
 * Created by jhansi on 29/03/15.
 */
public class ScanFragment extends Fragment {
    private Paint mPaint;
    DrawingView dv ;
    LinearLayout mDrawingPad;
    Map<Integer, PointF> point;
    private Button scanButton;
    private ImageView sourceImageView;
    private FrameLayout sourceFrame;
    private PolygonView polygonView;
    private View view;
    private ProgressDialogFragment progressDialogFragment;
    private IScanner scanner;
    private Bitmap original;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof IScanner)) {
            throw new ClassCastException("Activity must implement IScanner");
        }
        this.scanner = (IScanner) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.scan_fragment_layout, null);
        init();
        return view;
    }

    public ScanFragment() {

    }

    private void init() {
        sourceImageView = (ImageView) view.findViewById(R.id.sourceImageView);
        scanButton = (Button) view.findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new ScanButtonClickListener());
        sourceFrame = (FrameLayout) view.findViewById(R.id.sourceFrame);
        polygonView = (PolygonView) view.findViewById(R.id.polygonView);
        sourceFrame.post(new Runnable() {
            @Override
            public void run() {
                original = getBitmap();
                if (original != null) {
                    setBitmap(original);
                }
            }
        });


        dv = new DrawingView(getActivity());
        mDrawingPad=(LinearLayout)view.findViewById(R.id.view_drawing_pad);
        mDrawingPad.addView(dv);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);
        mDrawingPad.setVisibility(View.GONE);
    }

    public class DrawingView extends View {
        int i=1;
        public int width;
        public  int height;
        private Bitmap  mBitmap;
        private Canvas mCanvas;
        private Path mPath;
        private Paint mBitmapPaint;
        Context context;
        private Paint circlePaint;
        private Path circlePath;

        public DrawingView(Context c) {
            super(c);
            context=c;
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            circlePaint = new Paint();
            circlePath = new Path();
            circlePaint.setAntiAlias(true);
            circlePaint.setColor(Color.BLUE);
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setStrokeJoin(Paint.Join.MITER);
            circlePaint.setStrokeWidth(4f);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            canvas.drawBitmap( mBitmap, 0, 0, mBitmapPaint);
            canvas.drawPath( mPath,  mPaint);
            canvas.drawPath( circlePath,  circlePaint);
        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        private void touch_start(float x, float y) {
            mPath.reset();
         /*   mPath.moveTo(x, y);
            mX = x;
            mY = y;*/
         if(i==1) {

             mPath.moveTo(point.get(0).x, point.get(0).y);
         }
           else if(i==2) {

                mPath.moveTo(point.get(1).x, point.get(1).y);
            }
         else if(i==3) {

             mPath.moveTo(point.get(3).x, point.get(3).y);
         }
         else if(i==4) {

             mPath.moveTo(point.get(2).x, point.get(2).y);
         }
       /*     mPath.moveTo(point.get(0).x,point.get(0).y);
            mPath.moveTo(point.get(1).x,point.get(1).y);

            mPath.moveTo(point.get(2).x,point.get(2).y);

            mPath.moveTo(point.get(3).x,point.get(3).y);*//*
*/

        }

        private void touch_move(float x, float y) {
            /*float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                mX = x;
                mY = y;

                circlePath.reset();
                circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
            }*/
        }

        private void touch_up() {
          //  mPath.lineTo(mX, mY);
            if(i==1) {
                i++;
                mPath.lineTo(point.get(1).x, point.get(1).y);
            }
            else if(i==2){
                i++;
                mPath.lineTo(point.get(3).x, point.get(3).y);

            }
            else if(i==3){
                i++;
                mPath.lineTo(point.get(2).x, point.get(2).y);

            }
            else if(i==4){
                i++;
                mPath.lineTo(point.get(0).x, point.get(0).y);

            }
            Log.e("DEBUG==","ep="+mX+"y="+mY);

            circlePath.reset();
            // commit the path to our offscreen
            mCanvas.drawPath(mPath,  mPaint);
            // kill this so we don't double draw
            mPath.reset();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        }
    }

    private Bitmap getBitmap() {
        Uri uri = getUri();
        try {
            Bitmap bitmap = Util.getBitmap(getActivity(), uri);
            getActivity().getContentResolver().delete(uri, null, null);
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Uri getUri() {
        Uri uri = getArguments().getParcelable(ScanConstants.SELECTED_BITMAP);
        return uri;
    }

    private void setBitmap(Bitmap original) {
        Bitmap scaledBitmap = scaledBitmap(original, sourceFrame.getWidth(), sourceFrame.getHeight());
       // Bitmap scaledBitmap= scaleDown(original,300,true);
        if(SharedPreference.getInstance(getActivity()).getBoolean(ScanConstants.CAMERA)){
            sourceImageView.setImageBitmap(scaledBitmap);
        }
        else {
            sourceImageView.setImageBitmap(scaledBitmap);
        }
        Bitmap tempBitmap = ((BitmapDrawable) sourceImageView.getDrawable()).getBitmap();
        Map<Integer, PointF> pointFs = getEdgePoints(tempBitmap);
        polygonView.setPoints(pointFs);
        polygonView.setVisibility(View.VISIBLE);
        int padding = (int) getResources().getDimension(R.dimen.scanPadding);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(tempBitmap.getWidth() + 2 * padding, tempBitmap.getHeight() + 2 * padding);
        layoutParams.gravity = Gravity.CENTER;
        polygonView.setLayoutParams(layoutParams);
    }

    private Map<Integer, PointF> getEdgePoints(Bitmap tempBitmap) {
        List<PointF> pointFs = getContourEdgePoints(tempBitmap);
        Map<Integer, PointF> orderedPoints = orderedValidEdgePoints(tempBitmap, pointFs);
        return orderedPoints;
    }
    public Bitmap rotateBitmap(Bitmap scaledBitmap){
        Matrix matrix = new Matrix();

        matrix.postRotate(90);
        Bitmap newBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap .getWidth(), scaledBitmap .getHeight(), matrix, true);
        return newBitmap;
    }
    private List<PointF> getContourEdgePoints(Bitmap tempBitmap) {
        float[] points = ((ScanActivity) getActivity()).getPoints(tempBitmap);
        float x1 = points[0];
        float x2 = points[1];
        float x3 = points[2];
        float x4 = points[3];

        float y1 = points[4];
        float y2 = points[5];
        float y3 = points[6];
        float y4 = points[7];

        List<PointF> pointFs = new ArrayList<>();
        pointFs.add(new PointF(x1, y1));
        pointFs.add(new PointF(x2, y2));
        pointFs.add(new PointF(x3, y3));
        pointFs.add(new PointF(x4, y4));
        return pointFs;
    }


    private Map<Integer, PointF> getOutlinePoints(Bitmap tempBitmap) {
        Map<Integer, PointF> outlinePoints = new HashMap<>();
        outlinePoints.put(0, new PointF(0, 0));
        outlinePoints.put(1, new PointF(tempBitmap.getWidth(), 0));
        outlinePoints.put(2, new PointF(0, tempBitmap.getHeight()));
        outlinePoints.put(3, new PointF(tempBitmap.getWidth(), tempBitmap.getHeight()));
        return outlinePoints;
    }

    private Map<Integer, PointF> orderedValidEdgePoints(Bitmap tempBitmap, List<PointF> pointFs) {
        Map<Integer, PointF> orderedPoints = polygonView.getOrderedPoints(pointFs);
        if (!polygonView.isValidShape(orderedPoints)) {
            orderedPoints = getOutlinePoints(tempBitmap);
        }
        return orderedPoints;
    }

    private class ScanButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Map<Integer, PointF> points = polygonView.getPoints();
            if (isScanPointsValid(points)) {
                new ScanAsyncTask(points).execute();
            } else {
                showErrorDialog();
            }
        }
    }

    private void showErrorDialog() {
        SingleButtonDialogFragment fragment = new SingleButtonDialogFragment(R.string.ok, getString(R.string.cantCrop), "Error", true);
        FragmentManager fm = getActivity().getFragmentManager();
        fragment.show(fm, SingleButtonDialogFragment.class.toString());
    }

    private boolean isScanPointsValid(Map<Integer, PointF> points) {
        return points.size() == 4;
    }

    private Bitmap scaledBitmap(Bitmap bitmap, int width, int height) {

        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()), new RectF(0, 0, width, height), Matrix.ScaleToFit.CENTER);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    }
    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

      /*  Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
*/

        Matrix matrix = new Matrix();

        matrix.postRotate(90);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(realImage,width,height,filter);

        Bitmap newBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap .getWidth(), scaledBitmap .getHeight(), matrix, true);

        return newBitmap;
    }
    private Map<Integer, PointF>  getScannedBitmap(Bitmap original, Map<Integer, PointF> points) {
        int width = original.getWidth();
        int height = original.getHeight();
        Log.e("DEBUG","Img height="+height+ " width="+width);

        Log.e("DEBUG", "sourceImageView.getWidth()="+sourceImageView.getWidth());
        Log.e("DEBUG", "sourceImageView.getHeight()="+sourceImageView.getHeight());
        Log.e("DEBUG", "POints="+points.toString());

        float xRatio = (float) original.getWidth() / sourceImageView.getWidth();
        float yRatio = (float) original.getHeight() / sourceImageView.getHeight();

        float x1 = (points.get(0).x) * xRatio;
        float x2 = (points.get(1).x) * xRatio;
        float x3 = (points.get(2).x) * xRatio;
        float x4 = (points.get(3).x) * xRatio;
        float y1 = (points.get(0).y) * yRatio;
        float y2 = (points.get(1).y) * yRatio;
        float y3 = (points.get(2).y) * yRatio;
        float y4 = (points.get(3).y) * yRatio;

        List<PointF> point = new ArrayList<PointF>();
        point.add(new PointF(x1, y1));
        point.add(new PointF(x2, y2));
        point.add(new PointF(x3, y3));
        point.add(new PointF(x4, y4));
        Log.e("DEBUG", "POints(" + x1 + "," + y1 + ")(" + x2 + "," + y2 + ")(" + x3 + "," + y3 + ")(" + x4 + "," + y4 + ")");
        PointF centerPoint = new PointF();
        int size = points.size();
        for (PointF pointF : point) {
            centerPoint.x += pointF.x / size;
            centerPoint.y += pointF.y / size;
        }
        Map<Integer, PointF> orderedPoints = new HashMap<>();
        for (PointF pointF : point) {
            int index = -1;
            if (pointF.x < centerPoint.x && pointF.y < centerPoint.y) {
                index = 0;
            } else if (pointF.x > centerPoint.x && pointF.y < centerPoint.y) {
                index = 1;
            } else if (pointF.x < centerPoint.x && pointF.y > centerPoint.y) {
                index = 2;
            } else if (pointF.x > centerPoint.x && pointF.y > centerPoint.y) {
                index = 3;
            }
            orderedPoints.put(index, pointF);
        }
        return orderedPoints;
      //  Log.d("", "POints(" + x1 + "," + y1 + ")(" + x2 + "," + y2 + ")(" + x3 + "," + y3 + ")(" + x4 + "," + y4 + ")");
       // Bitmap _bitmap = ((ScanActivity) getActivity()).getScannedBitmap(original, x1, y1, x2, y2, x3, y3, x4, y4);
    }

    private class ScanAsyncTask extends AsyncTask<Void, Void, Bitmap> {

        private Map<Integer, PointF> points;

        public ScanAsyncTask(Map<Integer, PointF> points) {
            this.points = points;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog(getString(R.string.scanning));
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
             point =  getScannedBitmap(original, points);

            Mat tmp = new Mat (original.getWidth(), original.getHeight(), CvType.CV_8UC1);
            Utils.bitmapToMat(original, tmp);
           Bitmap result= sendToServer(point,tmp);
        //    return null;
            return result;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            dismissDialog();
         //   mDrawingPad.setVisibility(View.VISIBLE);


            Uri uri = Util.getUri(getActivity(), bitmap);
            scanner.onImageCutFinish(uri);


        }
    }


    Bitmap sendToServer(Map<Integer, PointF> points, Mat tmp){
        TransformImage transformImag=new TransformImage();
        Log.e("DEBUG1","points=="+points.get(0).x+"points=="+points.get(0).y);
        Log.e("DEBUG2","points=="+points.get(1).x+"points=="+points.get(1).y);
        Log.e("DEBUG3","points=="+points.get(2).x+"points=="+points.get(2).y);
        Log.e("DEBUG4","points=="+points.get(3).x+"points=="+points.get(3).y);

        Mat mat= transformImag.applyTransform(points.get(0).x,points.get(0).y,
                points.get(1).x,points.get(1).y,points.get(2).x,points.get(2).y,points.get(3).x,points.get(3).y,tmp);
        Bitmap result = Bitmap.createBitmap(mat.cols(),mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat,result);
        return result;
    }
    protected void showProgressDialog(String message) {
        progressDialogFragment = new ProgressDialogFragment(message);
        FragmentManager fm = getFragmentManager();
        progressDialogFragment.show(fm, ProgressDialogFragment.class.toString());
    }

    protected void dismissDialog() {
        progressDialogFragment.dismissAllowingStateLoss();
    }

}