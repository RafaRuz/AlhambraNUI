package com.example.alhambranui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageView;

import com.gjiazhe.panoramaimageview.GyroscopeObserver;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.random;
import static java.lang.Math.sqrt;

public class PatioLeonesActivity extends AppCompatActivity implements SensorEventListener {
    /**
     * Puzzle
     */
    ArrayList<PuzzlePiece> pieces;
    PuzzlePiece selectedPiece;

    /**
     * Sensores
     */
    private SensorManager sensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    private GyroscopeObserver gyroscopeObserver;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    private float speed = 30;
    private int maxWidth;
    private int maxHeight;
    private int positionedPieces = 0;

    private boolean shake_active = false;

    private boolean nightModeEnabled = false;
    private Sensor lightSensor;
    private static final int lightSensorThreshold = 0;

    FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patio_leones);

        final RelativeLayout  layout = findViewById(R.id.PatioLeonesLayout);
        ImageView imageView = findViewById(R.id.PatioLeonesImageView);
        maxWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        maxHeight = Resources.getSystem().getDisplayMetrics().heightPixels;


        // run image related code after the view was laid out
        // to have all dimensions calculated
        imageView.post(new Runnable() {
            @Override
            public void run() {
                pieces = splitImage();
                int pos = (int)(random()*12);
                selectedPiece = pieces.get(pos);
                System.out.println(pos);
                System.out.println(selectedPiece);
                selectedPiece.bringToFront();
                selectedPiece.setBackgroundColor(0);
                //TouchListener touchListener = new TouchListener();
                for(PuzzlePiece piece : pieces) {
                    ImageView iv = new ImageView(getApplicationContext());
                    //piece.setOnTouchListener(touchListener);
                    layout.addView(piece);
                }
            }
        });

        // Inicializar sensores
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        gyroscopeObserver = new GyroscopeObserver();
        gyroscopeObserver.setMaxRotateRadian(Math.PI/3);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        fab = findViewById(R.id.fab_patio_leones);

        // Diálogo de ayuda
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("Listo", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                hideSystemUI();
            }
        });
        builder.setMessage("Inclina tu teléfono para mover las piezas sobre la pantalla.\nTermina el puzzle para completar la actividad.");
        // Create the AlertDialog
        final AlertDialog dialog = builder.create();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        hideSystemUI();

        // Get updates from the accelerometer and magnetometer at a constant rate.
        // To make batch operations more efficient and reduce power consumption,
        // provide support for delaying updates to the application.
        //
        // In this example, the sensor reporting delay is small enough such that
        // the application receives an update before the system checks the sensor
        // readings again.
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_GAME);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_GAME);
        }

        gyroscopeObserver.register(this);
        sensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Don't receive any more updates from either sensor.
        sensorManager.unregisterListener(this);

        gyroscopeObserver.unregister();
        sensorManager.unregisterListener(mShakeDetector);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //  Light sensors
        if (event.sensor.getType() == Sensor.TYPE_LIGHT){
            if (event.values[0] < lightSensorThreshold && !nightModeEnabled){
                nightModeEnabled = true;
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                Intent intent = new Intent(this, PatioLeonesActivity.class);
                finish();
                startActivity(intent);
            }
            else if (event.values[0] > lightSensorThreshold && nightModeEnabled){
                nightModeEnabled = false;
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Intent intent = new Intent(this, PatioLeonesActivity.class);
                finish();
                startActivity(intent);
            }
        }

        //  Orientation Sensors
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);
        }

        updateOrientationAngles();

        if(selectedPiece != null) {
            RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) selectedPiece.getLayoutParams();
            lParams.leftMargin += orientationAngles[2]*speed;
            lParams.topMargin -= orientationAngles[1]*speed;

            if( lParams.leftMargin < 0 )
                lParams.leftMargin = 0;
            if( lParams.leftMargin > maxWidth - selectedPiece.pieceWidth )
                lParams.leftMargin = maxWidth - selectedPiece.pieceWidth;
            if( lParams.topMargin < 0 )
                lParams.topMargin = 0;
            if( lParams.topMargin > maxHeight - selectedPiece.pieceHeight )
                lParams.topMargin = maxHeight - selectedPiece.pieceHeight;

            selectedPiece.setLayoutParams(lParams);

            int xDiff = abs(selectedPiece.xCoord - lParams.leftMargin);
            int yDiff = abs(selectedPiece.yCoord - lParams.topMargin);
            final double tolerance = sqrt(pow(selectedPiece.getWidth(), 2) + pow(selectedPiece.getHeight(), 2)) / 20;
            if (xDiff <= tolerance && yDiff <= tolerance) {
                lParams.leftMargin = selectedPiece.xCoord;
                lParams.topMargin = selectedPiece.yCoord;
                selectedPiece.canMove = false;
                selectedPiece.setLayoutParams(lParams);
                positionedPieces++;

                if (positionedPieces == 12 & !shake_active) {
                    shake_active = true;
                    Toast.makeText(this, "Actividad completada! \n Agita para salir", Toast.LENGTH_LONG).show();

                    // ShakeDetector activation
                    mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

                        @Override
                        public void onShake(int count) {
                            Toast.makeText(PatioLeonesActivity.this, "shaked!", Toast.LENGTH_SHORT).show();
                            setResult(Activity.RESULT_OK);
                            finish();
                        }
                    });
                }

                while(!selectedPiece.canMove && positionedPieces < 12){
                    selectedPiece = pieces.get((int) (random() * 12));
                }
                selectedPiece.bringToFront();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    public void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);
        // "magnetometerReading" now has up-to-date information.

        SensorManager.getOrientation(rotationMatrix, orientationAngles);
        // "orientationAngles" now has up-to-date information.
    }

    //  Divide la imagen
    private ArrayList<PuzzlePiece> splitImage() {
        int piecesNumber = 12;
        int rows = 4;
        int cols = 3;

        ImageView imageView = findViewById(R.id.PatioLeonesImageView);
        ArrayList<PuzzlePiece> pieces = new ArrayList<>(piecesNumber);

        // Get the scaled bitmap of the source image
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        int[] dimensions = getBitmapPositionInsideImageView(imageView);
        int scaledBitmapLeft = dimensions[0];
        int scaledBitmapTop = dimensions[1];
        int scaledBitmapWidth = dimensions[2];
        int scaledBitmapHeight = dimensions[3];

        int croppedImageWidth = scaledBitmapWidth - 2 * abs(scaledBitmapLeft);
        int croppedImageHeight = scaledBitmapHeight - 2 * abs(scaledBitmapTop);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledBitmapWidth, scaledBitmapHeight, true);
        Bitmap croppedBitmap = Bitmap.createBitmap(scaledBitmap, abs(scaledBitmapLeft), abs(scaledBitmapTop), croppedImageWidth, croppedImageHeight);

        // Calculate the with and height of the pieces
        int pieceWidth = croppedImageWidth/cols;
        int pieceHeight = croppedImageHeight/rows;

        // Create each bitmap piece and add it to the resulting array
        int yCoord = 0;
        for (int row = 0; row < rows; row++) {
            int xCoord = 0;
            for (int col = 0; col < cols; col++) {
                // calculate offset for each piece
                int offsetX = 0;
                int offsetY = 0;
                if (col > 0) {
                    offsetX = pieceWidth / 3;
                }
                if (row > 0) {
                    offsetY = pieceHeight / 3;
                }

                // apply the offset to each piece
                Bitmap pieceBitmap = Bitmap.createBitmap(croppedBitmap, xCoord - offsetX, yCoord - offsetY, pieceWidth + offsetX, pieceHeight + offsetY);
                PuzzlePiece piece = new PuzzlePiece(getApplicationContext());
                piece.setImageBitmap(pieceBitmap);
                piece.xCoord = xCoord - offsetX + imageView.getLeft();
                piece.yCoord = yCoord - offsetY + imageView.getTop();
                piece.pieceWidth = pieceWidth + offsetX;
                piece.pieceHeight = pieceHeight + offsetY;

                // this bitmap will hold our final puzzle piece image
                Bitmap puzzlePiece = Bitmap.createBitmap(pieceWidth + offsetX, pieceHeight + offsetY, Bitmap.Config.ARGB_8888);

                // draw path
                int bumpSize = pieceHeight / 4;
                Canvas canvas = new Canvas(puzzlePiece);
                Path path = new Path();
                path.moveTo(offsetX, offsetY);
                if (row == 0) {
                    // top side piece
                    path.lineTo(pieceBitmap.getWidth(), offsetY);
                } else {
                    // top bump
                    path.lineTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 3, offsetY);
                    path.cubicTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 6, offsetY - bumpSize, offsetX + (pieceBitmap.getWidth() - offsetX) / 6 * 5, offsetY - bumpSize, offsetX + (pieceBitmap.getWidth() - offsetX) / 3 * 2, offsetY);
                    path.lineTo(pieceBitmap.getWidth(), offsetY);
                }

                if (col == cols - 1) {
                    // right side piece
                    path.lineTo(pieceBitmap.getWidth(), pieceBitmap.getHeight());
                } else {
                    // right bump
                    path.lineTo(pieceBitmap.getWidth(), offsetY + (pieceBitmap.getHeight() - offsetY) / 3);
                    path.cubicTo(pieceBitmap.getWidth() - bumpSize,offsetY + (pieceBitmap.getHeight() - offsetY) / 6, pieceBitmap.getWidth() - bumpSize, offsetY + (pieceBitmap.getHeight() - offsetY) / 6 * 5, pieceBitmap.getWidth(), offsetY + (pieceBitmap.getHeight() - offsetY) / 3 * 2);
                    path.lineTo(pieceBitmap.getWidth(), pieceBitmap.getHeight());
                }

                if (row == rows - 1) {
                    // bottom side piece
                    path.lineTo(offsetX, pieceBitmap.getHeight());
                } else {
                    // bottom bump
                    path.lineTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 3 * 2, pieceBitmap.getHeight());
                    path.cubicTo(offsetX + (pieceBitmap.getWidth() - offsetX) / 6 * 5,pieceBitmap.getHeight() - bumpSize, offsetX + (pieceBitmap.getWidth() - offsetX) / 6, pieceBitmap.getHeight() - bumpSize, offsetX + (pieceBitmap.getWidth() - offsetX) / 3, pieceBitmap.getHeight());
                    path.lineTo(offsetX, pieceBitmap.getHeight());
                }

                if (col == 0) {
                    // left side piece
                    path.close();
                } else {
                    // left bump
                    path.lineTo(offsetX, offsetY + (pieceBitmap.getHeight() - offsetY) / 3 * 2);
                    path.cubicTo(offsetX - bumpSize, offsetY + (pieceBitmap.getHeight() - offsetY) / 6 * 5, offsetX - bumpSize, offsetY + (pieceBitmap.getHeight() - offsetY) / 6, offsetX, offsetY + (pieceBitmap.getHeight() - offsetY) / 3);
                    path.close();
                }

                // mask the piece
                Paint paint = new Paint();
                paint.setColor(0XFF000000);
                paint.setStyle(Paint.Style.FILL);

                canvas.drawPath(path, paint);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                canvas.drawBitmap(pieceBitmap, 0, 0, paint);

                // draw a white border
                Paint border = new Paint();
                border.setColor(0X80FFFFFF);
                border.setStyle(Paint.Style.STROKE);
                border.setStrokeWidth(8.0f);
                canvas.drawPath(path, border);

                // draw a black border
                border = new Paint();
                border.setColor(0X80000000);
                border.setStyle(Paint.Style.STROKE);
                border.setStrokeWidth(3.0f);
                canvas.drawPath(path, border);

                // set the resulting bitmap to the piece
                piece.setImageBitmap(puzzlePiece);

                pieces.add(piece);
                xCoord += pieceWidth;
            }
            yCoord += pieceHeight;
        }

        return pieces;
    }

    //  Calcula la redimensionalización aplicada a la imagen para dividirla correctamente
    private int[] getBitmapPositionInsideImageView(ImageView imageView) {
        int[] ret = new int[4];

        if (imageView == null || imageView.getDrawable() == null)
            return ret;

        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int actW = Math.round(origW * scaleX);
        final int actH = Math.round(origH * scaleY);

        ret[2] = actW;
        ret[3] = actH;

        // Get image position
        // We assume that the image is centered into ImageView
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (int) (imgViewH - actH)/2;
        int left = (int) (imgViewW - actW)/2;

        ret[0] = left;
        ret[1] = top;

        return ret;
    }

    //  Para pantalla completa
    public void hideSystemUI(){
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    /*
    //  TouchListener para poder mover las piezas
    public class TouchListener implements View.OnTouchListener {
        private float xDelta;
        private float yDelta;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            float x = motionEvent.getRawX();
            float y = motionEvent.getRawY();
            final double tolerance = sqrt(pow(view.getWidth(), 2) + pow(view.getHeight(), 2)) / 10;

            PuzzlePiece piece = (PuzzlePiece) view;
            if (!piece.canMove) {
                return true;
            }

            RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    xDelta = x - lParams.leftMargin;
                    yDelta = y - lParams.topMargin;
                    piece.bringToFront();
                    break;
                case MotionEvent.ACTION_MOVE:
                    lParams.leftMargin = (int) (x - xDelta);
                    lParams.topMargin = (int) (y - yDelta);
                    view.setLayoutParams(lParams);
                    break;
                case MotionEvent.ACTION_UP:
                    int xDiff = abs(piece.xCoord - lParams.leftMargin);
                    int yDiff = abs(piece.yCoord - lParams.topMargin);
                    if (xDiff <= tolerance && yDiff <= tolerance) {
                        lParams.leftMargin = piece.xCoord;
                        lParams.topMargin = piece.yCoord;
                        piece.setLayoutParams(lParams);
                        piece.canMove = false;
                        sendViewToBack(piece);
                    }
                    break;
            }

            return true;
        }

        public void sendViewToBack(final View child) {
            final ViewGroup parent = (ViewGroup)child.getParent();
            if (null != parent) {
                parent.removeView(child);
                parent.addView(child, 0);
            }
        }
    }
    */

}

class PuzzlePiece extends AppCompatImageView {
    public int xCoord;
    public int yCoord;
    public int pieceWidth;
    public int pieceHeight;
    public boolean canMove = true;

    public PuzzlePiece(Context context) {
        super(context);
    }
}
