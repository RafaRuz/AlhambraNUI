package com.example.alhambranui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.gjiazhe.panoramaimageview.GyroscopeObserver;
import com.gjiazhe.panoramaimageview.PanoramaImageView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class CarlosVActivity extends AppCompatActivity implements SensorEventListener {
    private static final int UMBRAL = 300;
    private static final int lightSensorThreshold = 0;
    private float y0_ini = -1;
    private float y1_ini = -1;
    private float y0_fin = -1;
    private float y1_fin = -1;
    private boolean reg = false;
    private boolean up = false;
    private boolean down = false;
    private int actual_img = 1;
    private boolean shake_active = false;

    private GyroscopeObserver gyroscopeObserver;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    private boolean nightModeEnabled = false;
    private Sensor lightSensor;

    FloatingActionButton fab;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carlos_v);

        // https://www.youtube.com/watch?v=LXiS0E9cQp4
        gyroscopeObserver = new GyroscopeObserver();
        gyroscopeObserver.setMaxRotateRadian(Math.PI/3);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        lightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        fab = findViewById(R.id.fab_carlosv);

        // Diálogo de ayuda
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setPositiveButton("Listo", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                hideSystemUI();
            }
        });
        builder.setMessage("Arrastra hacia arriba o hacia abajo con dos o más dedos para cambiar a una vista más moderna o más antigua. \nAccede a todas las vistas para completar la actividad.");
        // Create the AlertDialog
        final AlertDialog dialog = builder.create();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        showPanoramic(R.drawable.carlosv_1);
    }

    private void showPanoramic(int resId) {
        PanoramaImageView panoramaImageView = findViewById(R.id.panorama_image_view);
        panoramaImageView.setImageResource(resId);
        panoramaImageView.setGyroscopeObserver(gyroscopeObserver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gyroscopeObserver.register(this);
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        hideSystemUI();
    }

    @Override
    protected void onPause() {
        gyroscopeObserver.unregister();
        mSensorManager.unregisterListener(mShakeDetector);
        mSensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        // Al pulsar con dos dedos registramos la posición y inicial de cada dedo
        if (event.getPointerCount() >= 2 && action == MotionEvent.ACTION_POINTER_DOWN) {
            //Toast.makeText(this, "action pointer down", Toast.LENGTH_SHORT).show();
            y0_ini = event.getY(0);
            y1_ini = event.getY(1);
            reg = true;
        }
        // Mientras nos movamos actualizamos la posición y final
        if (action == MotionEvent.ACTION_MOVE && reg) {
            //Toast.makeText(this, "action move", Toast.LENGTH_SHORT).show();
            y0_fin = event.getY(0);
            y1_fin = event.getY(1);
        }
        // Si detectamos un movimiento adecuado cambiamos la imagen
        if (action == MotionEvent.ACTION_POINTER_UP && reg) {
            //Toast.makeText(this, "action up", Toast.LENGTH_SHORT).show();
            reg = false;
            // Movimiento hacia arriba
            if (y0_ini-y0_fin > UMBRAL && y1_ini-y1_fin > UMBRAL) {
                //Toast.makeText(this, "arriba", Toast.LENGTH_SHORT).show();
                if (actual_img == 1) {
                    showPanoramic(R.drawable.carlosv_2);
                    actual_img = 2;
                    up = true;
                }
                else if (actual_img == 0) {
                    showPanoramic(R.drawable.carlosv_1);
                    actual_img = 1;
                }
            }
            if (y0_fin-y0_ini > UMBRAL && y1_fin-y1_ini > UMBRAL) {
                //Toast.makeText(this, "abajo", Toast.LENGTH_SHORT).show();
                if (actual_img == 1) {
                    showPanoramic(R.drawable.carlosv_0);
                    actual_img = 0;
                    down = true;
                }
                else if (actual_img == 2) {
                    showPanoramic(R.drawable.carlosv_1);
                    actual_img = 1;
                }

            }
        }
        if (up && down && !shake_active) {
            shake_active = true;
            Toast.makeText(this, "Actividad completada! \n Agita para salir", Toast.LENGTH_LONG).show();

            // ShakeDetector activation
            mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

                @Override
                public void onShake(int count) {
                    //Toast.makeText(CarlosVActivity.this, "shaked!", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                }
            });
        }

        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT){
            if (event.values[0] < lightSensorThreshold && !nightModeEnabled){
                nightModeEnabled = true;
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                Intent intent = new Intent(this, CarlosVActivity.class);
                finish();
                startActivity(intent);
            }
            else if (event.values[0] > lightSensorThreshold && nightModeEnabled){
                nightModeEnabled = false;
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Intent intent = new Intent(this, CarlosVActivity.class);
                finish();
                startActivity(intent);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // Pantalla completa
    public void hideSystemUI(){
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

}