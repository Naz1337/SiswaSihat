package com.mad.satu_c.group_satu.siswasihat;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

public class FocusZoneActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "FocusZoneActivity";
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private boolean isProximitySensorPresent;
    private int focusDurationMinutes;
    private FocusSessionFragment focusSessionFragment;
    private boolean sessionStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_zone);
        Log.d(TAG, "FocusZoneActivity created.");

        initSensors();

        // Initial fragment to show the setup screen
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new FocusSetupFragment())
                .commit();
        }
    }

    private void initSensors() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            if (proximitySensor == null) {
                isProximitySensorPresent = false;
                Toast.makeText(this, "Proximity Sensor not available!", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Proximity sensor: NOT AVAILABLE on this device.");
            } else {
                isProximitySensorPresent = true;
                Log.d(TAG, "Proximity sensor: AVAILABLE. Max Range: " + proximitySensor.getMaximumRange() + " cm");
                Log.d(TAG, "Proximity sensor: Resolution: " + proximitySensor.getResolution() + " cm");
                Log.d(TAG, "Proximity sensor: Vendor: " + proximitySensor.getVendor());
                Log.d(TAG, "Proximity sensor: Version: " + proximitySensor.getVersion());
            }
        } else {
            isProximitySensorPresent = false;
            Toast.makeText(this, "Sensor service not available!", Toast.LENGTH_LONG).show();
            Log.e(TAG, "SensorManager: Sensor service not available!");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isProximitySensorPresent && sessionStarted) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "onResume: Proximity sensor listener REGISTERED.");
        } else if (isProximitySensorPresent && !sessionStarted) {
            Log.d(TAG, "onResume: Proximity sensor listener NOT registered (session not started yet).");
        } else {
            Log.d(TAG, "onResume: Proximity sensor NOT present, listener not registered.");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isProximitySensorPresent) {
            sensorManager.unregisterListener(this);
            Log.d(TAG, "onPause: Proximity sensor listener UNREGISTERED.");
        } else {
            Log.d(TAG, "onPause: Proximity sensor NOT present, no listener to unregister.");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            float distance = event.values[0];
            Log.d(TAG, "onSensorChanged: Proximity sensor distance: " + distance + " cm. Max Range: " + proximitySensor.getMaximumRange() + " cm.");

            if (distance < proximitySensor.getMaximumRange()) { // Object is close (phone face down)
                Log.d(TAG, "onSensorChanged: Phone is face down (distance < max range).");
                if (!sessionStarted) {
                    Log.d(TAG, "onSensorChanged: Session not started yet. Calling startFocusSession().");
                    startFocusSession();
                } else if (focusSessionFragment != null && !focusSessionFragment.isTimerRunning()) {
                    Log.d(TAG, "onSensorChanged: Session started, timer paused. Resuming timer.");
                    focusSessionFragment.resumeTimer();
                    Toast.makeText(this, "Resuming focus session!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "onSensorChanged: Phone face down, session already active or timer running.");
                }
            } else { // Object is far (phone picked up)
                Log.d(TAG, "onSensorChanged: Phone is picked up (distance >= max range).");
                if (sessionStarted && focusSessionFragment != null && focusSessionFragment.isTimerRunning()) {
                    Log.d(TAG, "onSensorChanged: Session active, timer running. Pausing timer.");
                    focusSessionFragment.pauseTimer();
                    Toast.makeText(this, "Put me down, you can do it!", Toast.LENGTH_LONG).show();
                } else {
                    Log.d(TAG, "onSensorChanged: Phone picked up, but session not active or timer not running.");
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used for proximity sensor
    }

    public void startInstructions(int duration) {
        this.focusDurationMinutes = duration;
        Log.d(TAG, "startInstructions: Transitioning to instructions fragment. Duration: " + focusDurationMinutes + " minutes.");
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, FocusInstructionsFragment.newInstance(focusDurationMinutes))
            .commit();
        sessionStarted = false; // Reset session started flag
        // Register sensor listener here so it's active when instructions are shown
        if (isProximitySensorPresent) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "startInstructions: Proximity sensor listener REGISTERED for instructions phase.");
        }
    }

    private void startFocusSession() {
        Log.d(TAG, "startFocusSession: Starting focus session. Duration: " + focusDurationMinutes + " minutes.");
        sessionStarted = true;
        focusSessionFragment = FocusSessionFragment.newInstance(focusDurationMinutes);
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, focusSessionFragment)
            .commit();
        focusSessionFragment.startTimer();
        Toast.makeText(this, "Focus session started!", Toast.LENGTH_SHORT).show();
        // Unregister sensor listener after session starts, as the fragment will manage its own state
        if (isProximitySensorPresent) {
            sensorManager.unregisterListener(this);
            Log.d(TAG, "startFocusSession: Proximity sensor listener UNREGISTERED after session started.");
        }
    }
}
