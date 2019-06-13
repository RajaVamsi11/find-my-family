package com.example.findmyfam;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Criteria;
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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.findmyfam.AccountActivity.LoginActivity;
import com.example.findmyfam.Location.UserLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback {


    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude;
    private double currentLongitude;
    private LocationListener locationListener;
    double latitude, longitude;
    LocationManager locationManager;
    Location location;
    private int mInterval = 5000; // 5 seconds by default, can be changed later
    private Handler mHandler;
    private String IMEI;
    private LocationManager locationManager2;
    private int MIN_TIME = 0;
    private Marker  marker,oldMarker;
    private int c = 0;
    private int MIN_DIST = 0;
    private LatLng latLng;
    private GoogleMap mMap;
    private FloatingActionButton fab;
    private ArrayList<String> NameList;
    private ArrayList<Long> ImeiList;
    DatabaseReference myRef;
    FirebaseDatabase database;


    private DatabaseReference databaseReference;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TinyDB tinydb = new TinyDB(this);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        ImeiList = new ArrayList<Long>();
        NameList = new ArrayList<String>();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Create custom dialog object
                final Dialog dialog = new Dialog(MainActivity.this);
                // Include dialog.xml file
                dialog.setContentView(R.layout.activity_add_imei);
                // Set dialog title
                dialog.setTitle("Add Info");

                // set values for custom dialog components - text, image and button
                final EditText imei = dialog.findViewById(R.id.IMEI);
                final EditText name = dialog.findViewById(R.id.Name);


                dialog.show();

                Button add = (Button) dialog.findViewById(R.id.addImei);
                // if decline button is clicked, close the custom dialog
                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "Name " + name.getText().toString() + " Imei " + imei.getText().toString(), Toast.LENGTH_SHORT).show();// Close dialog
                        if (tinydb.getListLong("IMEI") != null) {
                            if (tinydb.getListString("NAME") != null) {
                                ImeiList = tinydb.getListLong("IMEI");
                                NameList = tinydb.getListString("NAME");
                            }
                        }
                        ImeiList.add(Long.parseLong(imei.getText().toString()));
                        NameList.add(name.getText().toString());
                        tinydb.putListLong("IMEI", ImeiList);
                        tinydb.putListString("NAME", NameList);
                        database = FirebaseDatabase.getInstance();

                       myRef = database.getReference("users");
                       Log.i("child",ImeiList.get(0)+"");
                        myRef.child((ImeiList.get(0)).toString().trim()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                double lat,lng;
                                String date;
                                UserLocation location = dataSnapshot.getValue(UserLocation.class);
                                Log.i("Lat and long firebase",location.getLatitude()+" "+location.getLongitude());
                                lat=location.getLatitude();
                                lng=location.getLongitude();
                                date=location.getDate();
                                addMarkers(lat,lng,NameList.get(0)+" mahajan");



                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                // Failed to read value
                                Log.w("tmz", "Failed to read value.", error.toException());
                            }
                        });



                    }
                });

                Button cancel = (Button) dialog.findViewById(R.id.cancel);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ImeiList = tinydb.getListLong("IMEI");
                        NameList = tinydb.getListString("NAME");
                        Log.i("IMEI and NAme",ImeiList.get(ImeiList.size()-1)+"  "
                                +NameList.get(NameList.size()-1));


                    }
                });


            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync((OnMapReadyCallback) this);


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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.i("IMEI", telephonyManager.getImei(0) + "");
        }
        Log.i("IMEI", telephonyManager.getDeviceId(1) + "");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            IMEI = telephonyManager.getImei(0);
        } else
            IMEI = telephonyManager.getDeviceId(0);
        databaseReference = FirebaseDatabase.getInstance().getReference();

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


    private void writeToFirebase(String IMEI, double lat, double longi, String date) {

        UserLocation userLocation = new UserLocation(lat, longi, date);
        databaseReference.child("users").child(IMEI).setValue(userLocation);
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
            try {
                search();
                mInterval = 5000; //this function can change value of mInterval.
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, 5000);
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
        Criteria criteria = new Criteria();
        String bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();
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
        Log.i("Network Enabled", networkIsEnabled + "");
        Log.i("Network Enabled", gpsIsEnabled + "");
        Log.i("Network Enabled", WifiIsEnabled + "");

        locationManager.requestLocationUpdates(bestProvider, 1000, 1, (android.location.LocationListener) this);
        location = locationManager.getLastKnownLocation(bestProvider);
        if (gpsIsEnabled) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (android.location.LocationListener) this);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (networkIsEnabled) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (android.location.LocationListener) this);
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (WifiIsEnabled) {
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, (android.location.LocationListener) this);
            location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }

        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            DateFormat df = new SimpleDateFormat("dd MMM yyyy, HH:mm");
            String date = df.format(Calendar.getInstance().getTime());
            Log.i("Last location", location.getLatitude() + "  " + location.getLongitude());
            writeToFirebase(IMEI, location.getLatitude(), location.getLongitude(), date);
            Toast.makeText(getApplicationContext(), location.getLatitude() + " " + location.getLongitude(), Toast.LENGTH_SHORT).show();
           // addMarkers(latitude, longitude,"MyLocation");
        }

    }


    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera


        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

//                try {if(marker!=null)
//                    {marker.remove();}
//                    latLng = new LatLng(location.getLatitude(), location.getLongitude());
//                    marker = mMap.addMarker(new MarkerOptions().position(latLng).title("My Location :"));
//                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//
//
//                }catch (SecurityException e)
//                {e.printStackTrace();}
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
        };

        locationManager2 = (LocationManager) getSystemService(LOCATION_SERVICE);

        try {
            boolean gpsIsEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean networkIsEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            boolean WifiIsEnabled = locationManager
                    .isProviderEnabled(LocationManager.PASSIVE_PROVIDER);

            if (gpsIsEnabled) {
                locationManager2.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
            }
            if (networkIsEnabled) {
                locationManager2.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
            }
            if (WifiIsEnabled) {
                locationManager2.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
            }


        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void addMarkers(double latitude, double longitude,String name) {
//Marker marker = null;
        if (mMap != null) {
            if (marker != null) {
                marker.remove();
            }
            latLng = new LatLng(latitude, longitude);
            marker = mMap.addMarker(new MarkerOptions().position(latLng).title(name));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        }
    }
}




