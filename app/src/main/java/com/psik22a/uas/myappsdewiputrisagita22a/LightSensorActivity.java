package com.psik22a.uas.myappsdewiputrisagita22a;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.button.MaterialButton;

public class LightSensorActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private TextView lightTextView;
    private TextView luminanceTextView;
    private CameraManager cameraManager;
    private String cameraId;
    private boolean isFlashOn = false; // Untuk melacak status senter
    private boolean isDialogShown = false; // Untuk melacak status dialog

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_sensor);

        // Initialize the TextView for light sensor display
        lightTextView = findViewById(R.id.lightTextView);
        luminanceTextView = findViewById(R.id.luminanceTextView);

        // Initialize CameraManager for controlling flashlight
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        // Initialize the SensorManager and light sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        // Check if the device has a light sensor
        if (lightSensor == null) {
            lightTextView.setText("Sensor cahaya tidak tersedia di perangkat ini.");
            luminanceTextView.setText(""); // Kosongkan TextView luminance
        }

        // Get the camera ID for the flashlight
        try {
            cameraId = cameraManager.getCameraIdList()[0]; // Ambil ID kamera untuk kontrol senter
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        // Button to go back to MainActivity
        MaterialButton btnBackToMain = findViewById(R.id.btnBackToMain);
        btnBackToMain.setOnClickListener(v -> {
            Intent intent = new Intent(LightSensorActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Optional: Close current activity
        });

        // Button to manually turn off the flashlight
        Button btnTurnOffFlashlight = findViewById(R.id.btnTurnOffFlashlight);
        btnTurnOffFlashlight.setOnClickListener(v -> {
            // Turn off flashlight when button is pressed
            turnOffFlashlight();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the sensor listener when the activity resumes
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the sensor listener when the activity pauses
        sensorManager.unregisterListener(this);
        isDialogShown = false; // Reset dialog status
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            // Get the current light level
            float lightLevel = event.values[0];
            lightTextView.setText("Tingkat cahaya saat ini: " + lightLevel + " lx");

            // Calculate and display luminance information
            String luminanceInfo = getLuminanceInfo(lightLevel);
            luminanceTextView.setText(luminanceInfo);

            // Change theme based on light intensity
            if (lightLevel < 5) {
                // Switch to Dark Mode if light is low
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

                // Show confirmation dialog before turning on the flashlight
                if (!isDialogShown) {
                    showFlashlightConfirmationDialog();
                    isDialogShown = true; // Set flag to true after showing dialog
                }
            } else if (lightLevel > 200) {
                // Switch to Light Mode if light is high
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                turnOffFlashlight(); // Turn off flashlight when light level is high
                isDialogShown = false; // Reset flag when light level is high
            }
        }
    }

    private String getLuminanceInfo(float lightLevel) {
        if (lightLevel < 5) {
            return "Kondisi: Gelap";
        } else if (lightLevel >= 5 && lightLevel <= 100) {
            return "Kondisi: Sedang";
        } else {
            return "Kondisi: Terang";
        }
    }

    // Method to show a confirmation dialog before turning on the flashlight
    private void showFlashlightConfirmationDialog() {
        if (!isFlashOn) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Aktifkan Senter")
                    .setMessage("Cahaya rendah terdeteksi. Apakah Anda ingin menghidupkan senter?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        turnOnFlashlight(); // If user agrees, turn on the flashlight
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss(); // If user declines, just dismiss the dialog
                    })
                    .show();
        }
    }

    // Method to turn on the flashlight
    private void turnOnFlashlight() {
        if (!isFlashOn) {
            try {
                cameraManager.setTorchMode(cameraId, true); // Turn on flashlight
                isFlashOn = true;
                Toast.makeText(this, "Senter dihidupkan", Toast.LENGTH_SHORT).show();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to turn off the flashlight
    private void turnOffFlashlight() {
        if (isFlashOn) {
            try {
                cameraManager.setTorchMode(cameraId, false); // Turn off flashlight
                isFlashOn = false;
                Toast.makeText(this, "Senter dimatikan", Toast.LENGTH_SHORT).show();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No action needed for now
    }
}
