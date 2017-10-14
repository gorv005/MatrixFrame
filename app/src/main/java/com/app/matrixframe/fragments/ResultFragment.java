package com.app.matrixframe.fragments;

import android.app.Dialog;
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
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.matrixframe.R;
import com.app.matrixframe.ScanActivity;
import com.app.matrixframe.util.PolygonViewWithTwoPoint;
import com.app.matrixframe.util.ScanConstants;
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

public class ResultFragment extends Fragment {
    private Paint mPaint;
    DrawingView dv ;
    LinearLayout mDrawingPad;
    private View view;
    private ImageView scannedImageView;
    private Button doneButton;
    private Bitmap original;
    private FrameLayout sourceFrame;
    private Button originalButton;
    private Button MagicColorButton;
    private Button grayModeButton;
    private Button bwButton;
    private Bitmap transformed;
    private float mX1, mY1, mX2,mY2;

    private static ProgressDialogFragment progressDialogFragment;
    private PolygonViewWithTwoPoint polygonView;

    public ResultFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.result_layout, null);
        Toast.makeText(getActivity(),"Press back to edit the transformation",Toast.LENGTH_LONG).show();
        init();
        return view;
    }

    private void init() {
        scannedImageView = (ImageView) view.findViewById(R.id.scannedImage);
        originalButton = (Button) view.findViewById(R.id.original);
        originalButton.setOnClickListener(new OriginalButtonClickListener());
        MagicColorButton = (Button) view.findViewById(R.id.magicColor);
        MagicColorButton.setOnClickListener(new MagicColorButtonClickListener());
        grayModeButton = (Button) view.findViewById(R.id.grayMode);
        grayModeButton.setOnClickListener(new GrayButtonClickListener());
        bwButton = (Button) view.findViewById(R.id.BWMode);
        sourceFrame = (FrameLayout) view.findViewById(R.id.sourceFrame);

        bwButton.setOnClickListener(new BWButtonClickListener());
        polygonView = (PolygonViewWithTwoPoint) view.findViewById(R.id.polygonView);


        doneButton = (Button) view.findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new DoneButtonClickListener());
        sourceFrame.post(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = getBitmap();


                if (bitmap != null) {
                    setScannedImage(bitmap);
                }
            }
        });
      /*  dv = new DrawingView(getActivity());
        mDrawingPad=(LinearLayout)view.findViewById(R.id.view_drawing_pad);
        mDrawingPad.addView(dv);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);*/
    }
    public class DrawingView extends View {

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
         //   mBitmapPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC)) ;

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

            mPath.moveTo(x, y);
            mX = x;
            mY = y;
            mX1=x;
            mY1=y;
            Log.e("DEBUG==","p="+x+"y="+y);
        }

        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                mX = x;
                mY = y;

                circlePath.reset();
                circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
            }
        }

        private void touch_up() {
            mPath.lineTo(mX, mY);
            Log.e("DEBUG==","ep="+mX+"y="+mY);
            mX2=mX;
            mY2=mY;
            circlePath.reset();
            // commit the path to our offscreen
            mCanvas.drawPath(mPath,  mPaint);
            // kill this so we don't double draw
            mPath.reset();
            showDialogForLength();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
      //  mBitmapPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC)) ;

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
    public Bitmap rotateBitmap(Bitmap scaledBitmap){
        Matrix matrix = new Matrix();

        matrix.postRotate(90);
        Bitmap newBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap .getWidth(), scaledBitmap .getHeight(), matrix, true);
        return newBitmap;
    }

    private Bitmap getBitmap() {
        Uri uri = getUri();
        try {
            original = Util.getBitmap(getActivity(), uri);
            getActivity().getContentResolver().delete(uri, null, null);
            return original;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Uri getUri() {
        Uri uri = getArguments().getParcelable(ScanConstants.SCANNED_RESULT);
        return uri;
    }

    public void setScannedImage(Bitmap scannedImage) {

        Bitmap scaledBitmap = scaledBitmap(scannedImage, sourceFrame.getWidth(), sourceFrame.getHeight());
        // Bitmap scaledBitmap= scaleDown(original,300,true);




        scannedImageView.setImageBitmap(scaledBitmap);
       Bitmap tempBitmap = ((BitmapDrawable) scannedImageView.getDrawable()).getBitmap();
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
    private Bitmap scaledBitmap(Bitmap bitmap, int width, int height) {

        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()), new RectF(0, 0, width, height), Matrix.ScaleToFit.CENTER);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    }
    private Map<Integer, PointF> orderedValidEdgePoints(Bitmap tempBitmap, List<PointF> pointFs) {
        Map<Integer, PointF> orderedPoints = polygonView.getOrderedPoints(pointFs);

            orderedPoints = getOutlinePoints(tempBitmap);

        return orderedPoints;
    }

    private Map<Integer, PointF> getOutlinePoints(Bitmap tempBitmap) {
        Map<Integer, PointF> outlinePoints = new HashMap<>();
        outlinePoints.put(0, new PointF(tempBitmap.getWidth()/2-100, tempBitmap.getHeight()/2));
        outlinePoints.put(1, new PointF(tempBitmap.getWidth()/2+100, tempBitmap.getHeight()/2));
        outlinePoints.put(2, new PointF(0, tempBitmap.getHeight()));
        outlinePoints.put(3, new PointF(tempBitmap.getWidth(), tempBitmap.getHeight()));
        return outlinePoints;
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
    private class DoneButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

          //  showDialogForLength();
            TransformImage transformImage = new TransformImage();


            Map<Integer, PointF> points = polygonView.getPoints();
            Map<Integer, PointF> p = getScannedBitmap(original, points);
            Mat tmp = new Mat(original.getWidth(), original.getHeight(), CvType.CV_8UC1);
            Utils.bitmapToMat(original, tmp);
            double[] pointsOutput = transformImage.getDimensions(tmp, p.get(0).x, p.get(0).y, p.get(1).x,
                    p.get(1).y,150);

            double width=pointsOutput[0];
            getDailogConfirm("Width=" + Util.round(width,1) + " " + "Height=" + Util.round(pointsOutput[1],1), "");


        }
    }


    private Map<Integer, PointF> getScannedBitmap(Bitmap original, Map<Integer, PointF> points) {
        int width = original.getWidth();
        int height = original.getHeight();
        Log.e("DEBUG","Img height="+height+ " width="+width);

        Log.e("DEBUG", "sourceImageView.getWidth()="+scannedImageView.getWidth());
        Log.e("DEBUG", "sourceImageView.getHeight()="+scannedImageView.getHeight());
        Log.e("DEBUG", "POints="+points.toString());

        float xRatio = (float) original.getWidth() / scannedImageView.getWidth();
        float yRatio = (float) original.getHeight() / scannedImageView.getHeight();

        float x1 = (points.get(0).x) * xRatio;
        float x2 = (points.get(1).x) * xRatio;

        float y1 = (points.get(0).y) * yRatio;
        float y2 = (points.get(1).y) * yRatio;


        List<PointF> point = new ArrayList<PointF>();
        point.add(new PointF(x1, y1));
        point.add(new PointF(x2, y2));

        Log.e("DEBUG", "POints(" + x1 + "," + y1 + ")(" + x2 + "," + y2 + ")");

        Map<Integer, PointF> orderedPoints = new HashMap<>();
        int index = 0;
        for (PointF pointF : point) {
            orderedPoints.put(index, pointF);
            index++;
        }
        return orderedPoints;
        //  Log.d("", "POints(" + x1 + "," + y1 + ")(" + x2 + "," + y2 + ")(" + x3 + "," + y3 + ")(" + x4 + "," + y4 + ")");
        // Bitmap _bitmap = ((ScanActivity) getActivity()).getScannedBitmap(original, x1, y1, x2, y2, x3, y3, x4, y4);
    }


    void getDailogConfirm(String dataText, String titleText) {
        try {
            final Dialog dialog = new Dialog(getActivity());
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            //tell the Dialog to use the dialog.xml as it's layout description
            dialog.setContentView(R.layout.dialog_with_two_button);
            // dialog.setTitle("Android Custom Dialog Box");
            dialog.setCancelable(true);
            TextView dataTextTv = (TextView) dialog.findViewById(R.id.dialog_data_text);
            TextView titleTextTv = (TextView) dialog.findViewById(R.id.dialog_title_text);
            TextView cancelTv = (TextView) dialog.findViewById(R.id.dialog_cancel_text);
            TextView okTextTv = (TextView) dialog.findViewById(R.id.dialog_ok_text);

            cancelTv.setVisibility(View.GONE);
            dataTextTv.setText(dataText);
            titleTextTv.setText(titleText);

            cancelTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();

                }
            });

            okTextTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                   /* Uri uri = Util.getUri(getActivity(), original);
                    Intent data = new Intent();
                    data.putExtra(ScanConstants.SCANNED_RESULT, uri);
                    data.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    getActivity().setResult(Activity.RESULT_OK, data);
                    getActivity().finish();*/
                }
            });

            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void showDialogForLength() {
        try {
            final Dialog dialog = new Dialog(getActivity());
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            //tell the Dialog to use the dialog.xml as it's layout description
            dialog.setContentView(R.layout.custom_dialog);
            // dialog.setTitle("Android Custom Dialog Box");
            dialog.setCancelable(false);
            final EditText edt = (EditText) dialog.findViewById(R.id.edit1);
            TextView titleTextTv = (TextView) dialog.findViewById(R.id.dialog_title_text);
            TextView cancelTv = (TextView) dialog.findViewById(R.id.dialog_cancel_text);
            TextView okTextTv = (TextView) dialog.findViewById(R.id.dialog_ok_text);
            cancelTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            okTextTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(edt.getText().toString().length()>0) {

                        TransformImage transformImage = new TransformImage();

                      /*  List<PointF> point = new ArrayList<PointF>();
                        point.add(new PointF(mX1, mY1));
                        point.add(new PointF(mX2, mY2));
                        Map<Integer, PointF> orderedPoints = new HashMap<>();
                        int index = 0;
                        for (PointF pointF : point) {
                            orderedPoints.put(index, pointF);
                            index++;
                        }*/

                        Map<Integer, PointF> points = polygonView.getPoints();
                        Map<Integer, PointF> p = getScannedBitmap(original, points);
                        Mat tmp = new Mat(original.getWidth(), original.getHeight(), CvType.CV_8UC1);
                        Utils.bitmapToMat(original, tmp);
                        double[] pointsOutput = transformImage.getDimensions(tmp, p.get(0).x, p.get(0).y, p.get(1).x,
                                p.get(1).y, Double.parseDouble(edt.getText().toString()));

                          double width=pointsOutput[0];
                        getDailogConfirm("Width=" + Util.round(width,1) + " " + "Height=" + Util.round(pointsOutput[1],1), "");
                        dialog.dismiss();
                    }
                    else {
                        edt.setError("Please enter scale length");

                    }

                }
            });

            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private class BWButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
           /* showProgressDialog(getResources().getString(R.string.applying_filter));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        transformed = ((ScanActivity) getActivity()).getBWBitmap(original);
                    } catch (final OutOfMemoryError e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                transformed = original;
                                scannedImageView.setImageBitmap(original);
                                e.printStackTrace();
                                dismissDialog();
                                onClick(v);
                            }
                        });
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scannedImageView.setImageBitmap(transformed);
                            dismissDialog();
                        }
                    });
                }
            });*/
        }
    }

    private class MagicColorButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
           /* showProgressDialog(getResources().getString(R.string.applying_filter));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        transformed = ((ScanActivity) getActivity()).getMagicColorBitmap(original);
                    } catch (final OutOfMemoryError e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                transformed = original;
                                scannedImageView.setImageBitmap(original);
                                e.printStackTrace();
                                dismissDialog();
                                onClick(v);
                            }
                        });
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scannedImageView.setImageBitmap(transformed);
                            dismissDialog();
                        }
                    });
                }
            });*/
        }
    }

    private class OriginalButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
           /* try {
                showProgressDialog(getResources().getString(R.string.applying_filter));
                transformed = original;
                scannedImageView.setImageBitmap(original);
                dismissDialog();
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                dismissDialog();
            }*/
        }
    }

    private class GrayButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
/*            showProgressDialog(getResources().getString(R.string.applying_filter));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        transformed = ((ScanActivity) getActivity()).getGrayBitmap(original);
                    } catch (final OutOfMemoryError e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                transformed = original;
                                scannedImageView.setImageBitmap(original);
                                e.printStackTrace();
                                dismissDialog();
                                onClick(v);
                            }
                        });
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scannedImageView.setImageBitmap(transformed);
                            dismissDialog();
                        }
                    });
                }
            });*/
        }
    }

    protected synchronized void showProgressDialog(String message) {
        if (progressDialogFragment != null && progressDialogFragment.isVisible()) {
            // Before creating another loading dialog, close all opened loading dialogs (if any)
            progressDialogFragment.dismissAllowingStateLoss();
        }
        progressDialogFragment = null;
        progressDialogFragment = new ProgressDialogFragment(message);
        FragmentManager fm = getFragmentManager();
        progressDialogFragment.show(fm, ProgressDialogFragment.class.toString());
    }

    protected synchronized void dismissDialog() {
        progressDialogFragment.dismissAllowingStateLoss();
    }
}