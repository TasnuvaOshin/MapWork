package com.khudrosoft.myapplication.Navigation;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.khudrosoft.myapplication.Direction.DirectionHelper.FetchURL;
import com.khudrosoft.myapplication.Direction.DirectionHelper.TaskLoadedCallback;
import com.khudrosoft.myapplication.R;
//this is the route working file

public class NavActivity extends AppCompatActivity implements OnMapReadyCallback,TaskLoadedCallback {
 private    GoogleMap gMap;
private Button navbutton;

private MarkerOptions origin,destination;
private Polyline currentLine;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);

        //to get the map fragment ready
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync((OnMapReadyCallback) this);
        //staring route
        origin = new MarkerOptions().position(new LatLng(27.658143,85.319950)).title("origin");
        destination = new MarkerOptions().position(new LatLng(27.667491,85.32085)).title("destination");

        //we need the api url for getting the route
        String url = getUrl(origin.getPosition(),destination.getPosition(),"driving");

        /*
        Fetchurl is the class that will get the value from the url
         */

        new FetchURL(NavActivity.this).execute(url,"driving");

    }

    private String getUrl(LatLng origin, LatLng destination, String driving) {


        String start = "origin="+origin.latitude+","+origin.longitude;

        String end ="destination="+destination.latitude+","+destination.longitude;
        String mode = "mode="+driving;
        String format = start+"&"+end+"&"+mode;
        String apiUrl = "https://maps.googleapis.com/maps/api/directions/json?"+format+"&key=AIzaSyAycZ7yEq_SjhoFHcq60NptoLBTN-f2lwc";
        return apiUrl;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //when map is readdy
         gMap = googleMap;  //assign to gobal variable

          gMap.addMarker(origin);
          gMap.addMarker(destination);

    }

    @Override
    public void onTaskDone(Object... values) {
        if(currentLine != null){
            currentLine.remove();
        }else {

            currentLine = gMap.addPolyline((PolylineOptions) values[0]);

        }
    }
}
