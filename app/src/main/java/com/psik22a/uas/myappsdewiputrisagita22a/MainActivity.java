package com.psik22a.uas.myappsdewiputrisagita22a;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MainActivity extends AppCompatActivity {

    private MapView mapView;
    private TextView speedText, altitudeText;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // OSMDroid configuration
        Configuration.getInstance().setUserAgentValue(getPackageName());

        // Initialize MapView
        mapView = findViewById(R.id.map);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(20.0);

        // Initialize TextViews for speed and altitude
        speedText = findViewById(R.id.speedText);
        altitudeText = findViewById(R.id.altitudeText);

        // Initialize FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Create LocationRequest
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(2000); // Update interval 10 seconds
        locationRequest.setFastestInterval(1000); // Fastest update interval 5 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // Use high accuracy

        // Check location permissions and request if not granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // Initialize LocationCallback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull com.google.android.gms.location.LocationResult locationResult) {
                if (locationResult != null) {
                    for (Location location : locationResult.getLocations()) {
                        if (location != null) {
                            updateLocation(location);
                        }
                    }
                }
            }
        };

        // Start location updates
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

        // FloatingActionButton for refreshing location
        FloatingActionButton fabRefresh = findViewById(R.id.fabRefresh);
        fabRefresh.setOnClickListener(view -> {
            // Refresh the location when FAB is clicked
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            }
        });

        Button btnTemperature = findViewById(R.id.btnTemperature);
        btnTemperature.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LightSensorActivity.class);
            startActivity(intent);
        });
    }

    private void updateLocation(Location location) {
        // Get latitude, longitude, speed, and altitude
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        double speed = location.getSpeed(); // Kecepatan dalam m/s
        double altitude = location.getAltitude();

        // Convert speed to km/h and round to the nearest whole number
        int speedKmPerHour = (int) Math.round(speed * 3.6); // Konversi m/s ke km/h dan bulatkan

        // Update UI with the retrieved data
        speedText.setText("Kecepatan: " + speedKmPerHour + " km/hour"); // Tampilkan kecepatan dalam km/hour
        altitudeText.setText("Ketinggian: " + altitude + " meter");

        // Set map center and marker at current location
        GeoPoint userLocation = new GeoPoint(latitude, longitude);
        mapView.getController().setCenter(userLocation);

        // Add marker for user's location
        Marker startMarker = new Marker(mapView);
        startMarker.setPosition(userLocation);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setTitle("Lokasi Anda");
        mapView.getOverlays().clear();
        mapView.getOverlays().add(startMarker);
        mapView.invalidate(); // Refresh the map view
    }



    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, start location updates
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop location updates when activity is destroyed
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
}
