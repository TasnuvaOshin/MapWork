package com.khudrosoft.myapplication.MyMap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.khudrosoft.myapplication.R;

import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleApiClient.OnConnectionFailedListener{
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    LocationManager locationManager;
    String provider;
    Location location;
    private static final float DEFAULT_ZOOM = 15f;
    SupportMapFragment mapFragment;
    private AutoCompleteTextView et_Search;
    private ImageButton ib_Search;
    private String search_text;
    private ArrayList<Address> addressArrayList;
    private PlaceAutocompleteAdapter placeAutocompleteAdapter;
    private  static  GoogleApiClient googleApiClient;
    private static LatLngBounds latLngBounds;
    protected GeoDataClient mGeoDataClient;
    private PlaceAutocompleteAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        googleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API).addApi(Places.PLACE_DETECTION_API).enableAutoManage(this,this).build();

   //     mGeoDataClient = Places.getGeoDataClient(this, null);
        setContentView(R.layout.activity_map);
        et_Search = findViewById(R.id.et_search);
        ib_Search = findViewById(R.id.ib_search);
        latLngBounds = new LatLngBounds(new LatLng(-40,-168),new LatLng(71,136));

        keyListener();
        //location Setup

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (checkLocationPermission()) {
            mapFragment.getMapAsync(this);
            keyListener();
        }


        //either user can press the search button key
        ib_Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                HideKeyBoard();
                SearchTheArea();
            }
        });

        //or user can do this with keyborad


    }

    private void SearchTheArea() {
        search_text = et_Search.getText().toString();

        Geocoder geocoder = new Geocoder(MapActivity.this);
        addressArrayList = new ArrayList<Address>();

        try {
            addressArrayList = (ArrayList<Address>) geocoder.getFromLocationName(search_text, 1);


        } catch (IOException e) {
            e.printStackTrace();
        }


        if (addressArrayList.size() > 0) {
            Address address = addressArrayList.get(0);
            UpdateCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));
            et_Search.getText().clear();
            addressArrayList.clear();
            Log.d("debug", "Search Area is working");
        }
    }


    private void keyListener() {
        // Set up the adapter that will retrieve suggestions from the Places Geo Data Client.
//        mAdapter = new PlaceAutocompleteAdapter(this, mGeoDataClient, latLngBounds, null);
//       et_Search.setAdapter(mAdapter);


  placeAutocompleteAdapter = new PlaceAutocompleteAdapter(this,googleApiClient,latLngBounds,null);
et_Search.setAdapter(placeAutocompleteAdapter);




        et_Search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                //int i this is the key that we need to trace the code from there

                if (i == EditorInfo.IME_ACTION_SEARCH || i == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent.ACTION_DOWN || i == KeyEvent.KEYCODE_ENTER) {


                    SearchTheArea();
                    HideKeyBoard();
                }


                return false;
            }
        });

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (checkLocationPermission()) {
            mMap = googleMap;
            GetMyLocation();
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
        }
    }

    public void GetMyLocation() {
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


    private void UpdateCamera(LatLng latLng, float zoom, String title) {
        if (checkLocationPermission()) {
            Log.d("debug", "We will now move the camera for current place");

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(title);
            mMap.addMarker(markerOptions);
        }

    }


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
                                ActivityCompat.requestPermissions(MapActivity.this,
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        //Request location updates:
                        mapFragment.getMapAsync(this);

                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }
    }


    public void HideKeyBoard() {

        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {


    }
}
