package com.mad.satu_c.group_satu.siswasihat;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;
import java.util.Random;

// Implement SensorEventListener to listen for sensor changes
public class DashboardActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "DashboardActivity";
    private RecyclerView recyclerView;
    private Button buttonLogout;
    private String username;

    // Sensor-related variables
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 800; // Adjust this value to change sensitivity
    private String[] motivationQuotes;
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        username = getIntent().getStringExtra("USERNAME");
        if (username == null) {
            Toast.makeText(this, "Error: Username not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        initListeners();
        initSensor(); // Initialize the sensor
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        buttonLogout = findViewById(R.id.buttonLogout);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        int spacing = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacing, true));
        String[] features = getResources().getStringArray(R.array.dashboard_features);
        DashboardAdapter adapter = new DashboardAdapter(this, features, username);
        recyclerView.setAdapter(adapter);

        // Load motivational quotes from resources
        motivationQuotes = getResources().getStringArray(R.array.motivational_quotes);
    }

    private void initListeners() {
        buttonLogout.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Initializes the accelerometer sensor.
     */
    private void initSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer == null) {
                // Device does not have an accelerometer
                Toast.makeText(this, "Accelerometer sensor not available.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the sensor listener when the activity is resumed
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "Accelerometer listener registered.");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the sensor listener when the activity is paused to save battery
        if (accelerometer != null) {
            sensorManager.unregisterListener(this);
            Log.d(TAG, "Accelerometer listener unregistered.");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            // Only check for shake every 100ms
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    Log.d(TAG, "Shake detected with speed: " + speed);
                    showMotivationQuote();
                }

                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used for this feature
    }

    /**
     * Selects a random quote and displays it in a Toast.
     */
    private void showMotivationQuote() {
        if (motivationQuotes != null && motivationQuotes.length > 0) {
            String quote = motivationQuotes[random.nextInt(motivationQuotes.length)];
            Toast.makeText(this, quote, Toast.LENGTH_LONG).show();
        }
    }
}