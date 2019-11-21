package com.example.alhambranui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


// https://stackoverflow.com/questions/43318968/how-to-make-a-simple-tracking-android-app-using-android-studio
public class MapsActivity extends FragmentActivity implements LocationListener,
        OnMapReadyCallback, GoogleApiClient
                .ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final boolean actualizar_localizacion = true;
    private final boolean marcadores_visibles = true;

    private GoogleMap mMap;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private Handler handler;
//    private GoogleApiClient googleApiClient;

    public final static int SENDING = 1;
    public final static int CONNECTING = 2;
    public final static int ERROR = 3;
    public final static int SENT = 4;
    public final static int SHUTDOWN = 5;

    private static final String TAG = "LocationActivity";
    private static final long INTERVAL = 1000 * 6;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    String mLastUpdateTime;
    private Location previousLocation;

    private Vibrator vibrator;
    private boolean one_vibration;

    // Localizaciones
    private static final LatLng CARLOS_V = new LatLng(37.176829, -3.589939);
    private static final LatLng ETSIIT = new LatLng(37.1970881, -3.6249585);
    private static final LatLng PATIO_LEONES = new LatLng(37.1771016,-3.5893965);
    private static final LatLng MOSAICOS = new LatLng(37.1774461,-3.5896342);

    // Marcadores
    private static Marker carlos_v_marker;
    private static Marker patio_leones_marker;
    private static Marker mosaicos_marker;

    // Request codes
    private static final int CARLOS_V_REQ = 5;
    private static final int PATIO_LEONES_REQ = 6;
    private static final int MOSAICOS_REQ = 7;
    private Object MapsActivity;

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {

                    case SENDING:

                        break;

                }

            }
        };

        one_vibration = false;
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  // pantalla en vertical siempre

        //checkLocationPermission();
    }


    // Se llama cuando el usuario termina con una actividad y regresa
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CARLOS_V_REQ:
                if (resultCode == RESULT_OK) {
                    carlos_v_marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    carlos_v_marker.setSnippet("Actividad completada");

                    boolean[] aux = MainActivity.Companion.getInsignias();
                    aux[0] = true;
                    MainActivity.Companion.setInsignias(aux);
                }
                break;
            case PATIO_LEONES_REQ:
                if (resultCode == RESULT_OK) {
                    patio_leones_marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    patio_leones_marker.setSnippet("Actividad completada");

                    boolean[] aux = MainActivity.Companion.getInsignias();
                    aux[1] = true;
                    MainActivity.Companion.setInsignias(aux);
                }
                break;
            case MOSAICOS_REQ:
                if (resultCode == RESULT_OK || resultCode == RESULT_CANCELED) {
                    System.out.println("*********************" + resultCode + "****************************");
                    mosaicos_marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    mosaicos_marker.setSnippet("Actividad completada");

                    boolean[] aux = MainActivity.Companion.getInsignias();
                    aux[2] = true;
                    MainActivity.Companion.setInsignias(aux);
                }
                else {System.out.println("*********************" + resultCode + "****************************");}
                break;
        }
    }

    // Distancia en metros  a partir de latitud-longitud
    public static double distFrom(LatLng orig, LatLng dest) {
        double lat1 = orig.latitude;
        double lat2 = dest.latitude;
        double lng1 = orig.longitude;
        double lng2 = dest.longitude;
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = earthRadius * c;

        return dist;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }


        carlos_v_marker = mMap.addMarker(new MarkerOptions().position(CARLOS_V).title("Palacio de Carlos V").snippet("Pulsa para empezar prueba").icon(BitmapDescriptorFactory.fromResource(R.drawable.carlosv)).visible(marcadores_visibles));
        patio_leones_marker = mMap.addMarker(new MarkerOptions().position(PATIO_LEONES).title("Patio de los Leones").snippet("Pulsa para empezar prueba").icon(BitmapDescriptorFactory.fromResource(R.drawable.patio_leones_icon)).visible(marcadores_visibles));
        mosaicos_marker = mMap.addMarker(new MarkerOptions().position(MOSAICOS).title("Mosaicos").snippet("Pulsa para empezar prueba").icon(BitmapDescriptorFactory.fromResource(R.drawable.mosaicos_icon)).visible(marcadores_visibles));
        mMap.addMarker(new MarkerOptions().position(ETSIIT).title("ETSIIT").visible(marcadores_visibles));

        // Pruebas (se activan cuando pulsamos el Marker)
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent();
                int request_code = -1;
                // Se llama a una actividad para cada prueba
                if (marker.getTitle().equals("Patio de los Leones")) {
                    intent = new Intent(MapsActivity.this, PatioLeonesActivity.class);
                    request_code = PATIO_LEONES_REQ;

                }
                else if (marker.getTitle().equals("Palacio de Carlos V")) {
                    intent = new Intent(MapsActivity.this, CarlosVActivity.class);
                    request_code = CARLOS_V_REQ;
                }
                else if (marker.getTitle().equals("Mosaicos")) {
                    intent = new Intent(MapsActivity.this, MosaicosActivity.class);
                    request_code = MOSAICOS_REQ;
                }

                startActivityForResult(intent, request_code);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                mMap.setMyLocationEnabled(true);
                /*
                Intent intent = new Intent(this, MapsActivity.class);
                //finish();
                startActivity(intent);
                */

            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Toast.makeText(this, "La app necesita usar la ubicación...", Toast.LENGTH_SHORT).show();
                finish();
            }
            return;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        if (actualizar_localizacion) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart fired ..............");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mGoogleApiClient.disconnect();
        Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: ");
    }


    private void pruebaCerca(String name) {
        if (!one_vibration) {
            vibrator.vibrate(500);
            one_vibration = true;
            Toast.makeText(this, "Actividad cerca: " + name, Toast.LENGTH_SHORT).show();
        }
    }

    LatLng latLng;

    @Override
    public void onLocationChanged(Location location) {
        //Toast.makeText(this, "location changed", Toast.LENGTH_SHORT).show();
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));

        previousLocation = location;
        Log.d(TAG, "Firing onLocationChanged..........................");
        Log.d(TAG, "lat :" + location.getLatitude() + "long :" + location.getLongitude());
        Log.d(TAG, "bearing :" + location.getBearing());

        // Comprobamos si estamos cerca de algún marcador
        if (distFrom(latLng, CARLOS_V) < 60) {
            pruebaCerca("Carlos V");
            carlos_v_marker.setVisible(true);
        }
        if (distFrom(latLng, PATIO_LEONES) < 60) {
            pruebaCerca("Patio Leones");
            patio_leones_marker.setVisible(true);
        }
        if (distFrom(latLng, MOSAICOS) < 60) {
            pruebaCerca("Mosaicos");
            mosaicos_marker.setVisible(true);
        }
        if (distFrom(latLng, ETSIIT) < 100) {
            pruebaCerca("Etsiit");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            stopLocationUpdates();
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            if (actualizar_localizacion &&
                    ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            }
            Log.d(TAG, "Location update resumed .....................");
        }
    }
}