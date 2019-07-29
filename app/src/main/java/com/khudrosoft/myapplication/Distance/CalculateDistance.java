package com.khudrosoft.myapplication.Distance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.khudrosoft.myapplication.MyMap.MapActivity;
import com.khudrosoft.myapplication.R;


import static com.khudrosoft.myapplication.MyMap.MapActivity.MY_PERMISSIONS_REQUEST_LOCATION;

public class CalculateDistance extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {
    private static final float DEFAULT_ZOOM = 15f;
    GoogleMap gMap;
    private Button bDistance;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location location;
    private Double StartLang, StartLong, EndLang, EndLong;

    /*

    **dont use emulator for test , use real device
    **Long Press to drag
    *** click at the middle to get the distance
    author : tasnuva
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate_distance);
        bDistance = findViewById(R.id.distance);


        //for map Setting
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync((OnMapReadyCallback) this);


        bDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MarkerOptions markerO = new MarkerOptions();
                markerO.position(new LatLng(EndLang, EndLong));
                markerO.title("destination");
                markerO.draggable(true);
                markerO.draggable(true);
                float results[] = new float[10];
                Location.distanceBetween(StartLang, StartLong, EndLang, EndLong, results);
                markerO.snippet("Distance=" + results[0]/1000+ "km");
                Log.d("d", String.valueOf(results[0]));
                //distance =results[0] / 1000; // in km
                gMap.addMarker(markerO);
            }
        });


    }

    @SuppressLint("NewApi")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (checkLocationPermission()) {
            gMap = googleMap;
            gMap.setOnMarkerClickListener(this);
            gMap.setOnMarkerDragListener(this);
            //get the current Location
            GetMyLocation();
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            gMap.setMyLocationEnabled(true);
        }


    }

    private void GetMyLocation() {
        if (checkLocationPermission()) {
            Log.d("debug", "Now We want to show our current location");
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            @SuppressLint("MissingPermission") Task LocationTask = fusedLocationProviderClient.getLastLocation();

            LocationTask.addOnCompleteListener(new OnCompleteListener() {

                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {

                        Log.d("debug", "Now We get our Current Location");

                        location = (Location) task.getResult();

                        UpdateCamera(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM, "Location");

                    }
                }
            });
        }


    }

    //this is for permission
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Permission")
                        .setMessage("Please Share/on Your Location")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(CalculateDistance.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }


    //now we will update the camera as per our location

    private void UpdateCamera(LatLng latLng, float zoom, String title) {
        if (checkLocationPermission()) {
            Log.d("debug", "We will now move the camera for current place");

            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(title);
            markerOptions.draggable(true);
            gMap.addMarker(markerOptions);

        }

    }

    //for distance
    private void UpdateCameraForDistance(LatLng latLng, float zoom, String title) {
        if (checkLocationPermission()) {
            Log.d("debug", "We will now move the camera for current place");
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(title);

        }

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.setDraggable(true);
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        //now we need to update our camera
        StartLang = marker.getPosition().latitude;
        StartLong = marker.getPosition().longitude;
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

        EndLang = marker.getPosition().latitude;
        EndLong = marker.getPosition().longitude;
       // UpdateCamera(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude), DEFAULT_ZOOM, "Location");

    }
}
