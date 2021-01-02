package com.example.memorableplaces;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    public void centerMapLocation(Location location,String title){
        if(location!=null) {
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(userLocation).title(title));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12));
        }
    }

    // action on location access allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
             if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                 locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,1000,locationListener);
             }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Intent intent = getIntent();
        int placeNumber = intent.getIntExtra("placeNumber" , 0);
        //placeNumber =0 then zoom in current location of user
        if(placeNumber==0){
            locationManager =(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener  = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    centerMapLocation(location,"Current Location");
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };
            // if permission given then move camera to latest location else request for permission
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,1000,locationListener);
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapLocation(lastLocation , "Current Location");
            }else{
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION} ,1);
            }

        }else{
            Location placeLocation = new Location(LocationManager.GPS_PROVIDER);
            placeLocation.setLatitude(MainActivity.locations.get(placeNumber).latitude);
            placeLocation.setLongitude(MainActivity.locations.get(placeNumber).longitude);
            centerMapLocation(placeLocation , MainActivity.places.get(placeNumber));
        }
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Geocoder geocoder = new Geocoder(getApplicationContext() , Locale.getDefault());
                String address = "";
                try{
                    List<Address> listAddresses = geocoder.getFromLocation(latLng.latitude , latLng.longitude,1);
                    if(listAddresses != null && listAddresses.size()>0){
                        if(listAddresses.get(0).getThoroughfare() != null){
                            if(listAddresses.get(0).getSubThoroughfare() != null ){
                                address += listAddresses.get(0).getSubThoroughfare();
                            }
                            address += listAddresses.get(0).getThoroughfare();
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                if(address.equals("")) {
                    SimpleDateFormat sdf = new SimpleDateFormat(" HH:mm yyyy-MM-dd");
                    address += sdf.format(new Date());
                }
                googleMap.addMarker(new MarkerOptions().position(latLng).title(address));
                MainActivity.places.add(address);
                MainActivity.locations.add(latLng);
                MainActivity.arrayAdapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "Location Saved ", Toast.LENGTH_SHORT).show();
            }
        });




    }
}