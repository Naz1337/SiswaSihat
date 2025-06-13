package com.mad.satu_c.group_satu.siswasihat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Locale;

public class SosHelpActivity extends Activity implements LocationListener {

    // TAG for logging, as per coding rules
    private static final String TAG = "SosHelpActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;

    // UI Views
    private TextView textViewCoordinates;
    private Button buttonCallHotline;
    private Button buttonShowOnMap;

    // Location
    private LocationManager locationManager;
    private Location lastKnownLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos_help);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        initViews();
        initListeners();
    }

    /**
     * Initializes all UI views from the XML layout.
     */
    private void initViews() {
        textViewCoordinates = findViewById(R.id.textViewCoordinates);
        buttonCallHotline = findViewById(R.id.buttonCallHotline);
        buttonShowOnMap = findViewById(R.id.buttonShowOnMap);

        // Set initial state for UI elements
        textViewCoordinates.setText(R.string.sos_coordinates_loading);
        buttonShowOnMap.setEnabled(false); // Disable map button until location is found
    }

    /**
     * Initializes all listeners for UI components.
     */
    private void initListeners() {
        buttonCallHotline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implicit intent to dial a hotline number
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + getString(R.string.sos_hotline_number)));
                // Check if an app can handle this intent
                if (dialIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(dialIntent);
                } else {
                    Toast.makeText(SosHelpActivity.this, R.string.sos_hotline_error, Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonShowOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastKnownLocation != null) {
                    // Implicit intent to show location on a map
                    String geoUri = String.format(Locale.ENGLISH, "geo:%f,%f",
                            lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
                    // Check if an app can handle this intent
                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(mapIntent);
                    } else {
                        Toast.makeText(SosHelpActivity.this, R.string.sos_map_error, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    /**
     * Checks for location permissions and starts listening for updates if granted.
     */
    private void checkAndRequestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    /**
     * Starts listening for location updates from available providers.
     */
    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return; // Should not happen, but a good safeguard.
        }

        try {
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGpsEnabled && !isNetworkEnabled) {
                Toast.makeText(this, "Please enable Location services", Toast.LENGTH_LONG).show();
                textViewCoordinates.setText(R.string.sos_location_unavailable);
                return;
            }

            // Request updates from network provider for a fast initial fix
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);
            }
            // Request updates from GPS for better accuracy
            if (isGpsEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException in startLocationUpdates: " + e.getMessage());
            Toast.makeText(this, "Failed to start location updates.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, R.string.sos_permission_denied, Toast.LENGTH_LONG).show();
                textViewCoordinates.setText(R.string.sos_permission_denied);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAndRequestLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop listening for location updates to save battery
        locationManager.removeUpdates(this);
    }

    // LocationListener Callbacks

    @Override
    public void onLocationChanged(@NonNull Location location) {
        lastKnownLocation = location;
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        String coordinates = String.format(Locale.getDefault(), "Lat: %.5f, Lon: %.5f", latitude, longitude);

        textViewCoordinates.setText(coordinates);

        // Enable the map button now that we have a location
        if (!buttonShowOnMap.isEnabled()) {
            buttonShowOnMap.setEnabled(true);
        }

        Log.d(TAG, "Location Updated: " + coordinates);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        // This might be called if user turns off GPS while app is running
        Log.w(TAG, "Provider disabled: " + provider);
        Toast.makeText(this, "A location provider was disabled.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Log.d(TAG, "Provider enabled: " + provider);
        textViewCoordinates.setText(R.string.sos_coordinates_loading);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Deprecated and not typically needed.
    }
}