package com.example.findmyfam;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

//import com.google.android.gms.location.LocationListener;

import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.example.findmyfam.AccountActivity.LoginActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements LocationListener {


    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude;
    private double currentLongitude;
    String latitude, longitude;
    LocationManager locationManager;
    Location location;
    private int mInterval = 5000; // 5 seconds by default, can be changed later
    private Handler mHandler;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Log.i("IMEI", telephonyManager.getDeviceId(0) + "");
        Log.i("IMEI", telephonyManager.getDeviceId(1) + "");


        mHandler = new Handler();
        startRepeatingTask();

//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                // The next two lines tell the new client that “this” current class will handle connection stuff
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                //fourth line adds the LocationServices API endpoint from GooglePlayServices
//                .addApi(LocationServices.API)
//                .build();
//
//        // Create the LocationRequest object
//        mLocationRequest = LocationRequest.create()
//                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//                .setInterval(0)        // 10 seconds, in milliseconds
//                .setFastestInterval(0);
//        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        startActivity(new Intent(MainActivity.this, LoginActivity.class));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {search();
                mInterval=5000; //this function can change value of mInterval.
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, 30000);
            }
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        //Now lets connect to the API
//        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(this.getClass().getSimpleName(), "onPause()");

        //Disconnect from API onPause()
//        if (mGoogleApiClient.isConnected()) {
//            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, MainActivity.this);
//            mGoogleApiClient.disconnect();
//        }


    }


    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void search() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        boolean gpsIsEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkIsEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean WifiIsEnabled = locationManager
                .isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
        Log.i("Network Enabled",networkIsEnabled+"");
        if(gpsIsEnabled)
        {locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (android.location.LocationListener) this);
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);}
        if(networkIsEnabled)
        {locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (android.location.LocationListener) this);
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);}
        if(WifiIsEnabled)
        {locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, (android.location.LocationListener) this);
            location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);}
        Log.i("Last location",location.getLatitude()+"  "+location.getLongitude());
        Toast.makeText(getApplicationContext(), location.getLatitude()+" "+location.getLongitude(), Toast.LENGTH_SHORT).show();
    }


    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

}

