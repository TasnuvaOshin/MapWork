package com.khudrosoft.myapplication.Direction;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.khudrosoft.myapplication.Direction.DirectionHelper.FetchURL;
import com.khudrosoft.myapplication.Direction.DirectionHelper.TaskLoadedCallback;
import com.khudrosoft.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class DirectionActivity extends AppCompatActivity implements OnMapReadyCallback,TaskLoadedCallback {
    private GoogleMap gMap;
    private MarkerOptions startPlace, endPlace;
    private Button directionButton;
    private Polyline currentPolyline;
    List<MarkerOptions> markerOptions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction);

        markerOptions = new ArrayList<>();
        directionButton = findViewById(R.id.map_direction);

        directionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                new FetchURL(DirectionActivity.this)
                        .execute(getUrl(startPlace.getPosition(), endPlace.getPosition(), "driving"));

            }
        });


        startPlace = new MarkerOptions().position(new LatLng(27.658143, 85.3199503)).title("starting place");
        endPlace = new MarkerOptions().position(new LatLng(27.788479, 85.3877425)).title("ending place");
        markerOptions.add(startPlace);
        markerOptions.add(endPlace);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    //for getting the route we need to call the GetUrl Method
    private String getUrl(LatLng Origin_position, LatLng Destination_position, String direction) {
        String Origin = "origin=" + Origin_position.latitude + "," + Origin_position.longitude;

        String Destination = "destination=" + Destination_position.latitude + "," + Destination_position.longitude;

        String mode = "mode=driving";

        String parameter = Origin + "&" + Destination + "&" + mode;
        //this is the main api key for direction

        String url = "https://maps.googleapis.com/maps/api/directions/json?" + parameter + "&key=AIzaSyAycZ7yEq_SjhoFHcq60NptoLBTN-f2lwc";
        //https://maps.googleapis.com/maps/api/directions/json?origin=23.7746465,90.3944036&destination=23.7746465,90.3944036&mode=driving&key=AIzaSyAhje3XitH37m7iMYmVh0U1hnzsGmCLaSI

        return url;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        gMap.addMarker(startPlace);
        gMap.addMarker(endPlace);


        ShowAllMapLine();
    }

    private void ShowAllMapLine() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (MarkerOptions mo : markerOptions) {
            builder.include(mo.getPosition());
        }


        LatLngBounds bounds = builder.build();
        int width = getResources().getDisplayMetrics().widthPixels;

        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.30);


        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        gMap.animateCamera(cameraUpdate);
    }

    @Override
    public void onTaskDone(Object... values) {
        if(currentPolyline !=null){

            currentPolyline.remove();


            currentPolyline = gMap.addPolyline((PolylineOptions) values[0]);
        }
    }
}
