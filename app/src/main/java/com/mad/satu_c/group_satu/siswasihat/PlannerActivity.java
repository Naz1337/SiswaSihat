package com.mad.satu_c.group_satu.siswasihat;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import java.util.Map;

public class PlannerActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "PlannerActivity";
    private String username;
    private FirebaseFirestore db;
    private List<Map<String, Object>> eventsList;
    private ArrayAdapter<String> adapter;
    private ListView listViewEvents;
    private Button buttonAddEvent;

    // Step Counter variables
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private boolean isStepSensorPresent;
    private int currentSteps = 0;
    private long lastResetDay = 0;

    // Enhanced sensor support
    private boolean useStepCounter = false;
    private boolean useAccelerometer = false;
    private int initialStepCount = 0; // For TYPE_STEP_COUNTER
    private boolean isFirstStepCounterReading = true;

    // Accelerometer-based step detection
    private float lastAccelValue = 0;
    private float currentAccelValue = 0;
    private float shake = 0;
    private boolean stepDetected = false;
    private long lastStepTime = 0;
    private static final float STEP_THRESHOLD = 12.0f;
    private static final long STEP_DELAY_MS = 500; // Minimum time between steps

    private TextView textViewStepsToday;
    private TextView textViewStepGoal;
    private ProgressBar progressBarSteps;
    private Button buttonSetStepGoal;
    private TextView textViewSensorStatus;
    private Button buttonManualStep; // For testing/manual increment

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "StepCounterPrefs";
    private static final String PREF_CURRENT_STEPS = "currentSteps";
    private static final String PREF_LAST_RESET_DAY = "lastResetDay";
    private static final String PREF_STEP_GOAL = "stepGoal";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planner);

        // Get username from intent
        username = getIntent().getStringExtra("USERNAME");
        if (username == null) {
            Toast.makeText(this, "Error: Username not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Initialize views and adapter
        initViews();
        initListeners();

        // Load events
        loadEvents();

        // Initialize Step Counter with multiple sensor support
        initStepCounter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isStepSensorPresent) {
            registerStepSensor();
        }
        loadStepData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isStepSensorPresent && sensorManager != null) {
            sensorManager.unregisterListener(this);
            Log.d(TAG, "Step sensor listener unregistered in onPause()");
        }
        saveStepData();
    }

    private void initViews() {
        listViewEvents = findViewById(R.id.listViewEvents);
        buttonAddEvent = findViewById(R.id.buttonAddEvent);
        eventsList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        listViewEvents.setAdapter(adapter);

        // Initialize Step Counter UI elements
        textViewStepsToday = findViewById(R.id.textViewStepsToday);
        textViewStepGoal = findViewById(R.id.textViewStepGoal);
        progressBarSteps = findViewById(R.id.progressBarSteps);
        buttonSetStepGoal = findViewById(R.id.buttonSetStepGoal);
        textViewSensorStatus = findViewById(R.id.textViewSensorStatus);

        // Add manual step button (you'll need to add this to your layout)
        // buttonManualStep = findViewById(R.id.buttonManualStep);
    }

    private void initListeners() {
        buttonAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddEventDialog(null);
            }
        });

        listViewEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> event = eventsList.get(position);
                showAddEventDialog(event);
            }
        });

        listViewEvents.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> event = eventsList.get(position);
                showDeleteConfirmationDialog(event);
                return true;
            }
        });

        buttonSetStepGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSetStepGoalDialog();
            }
        });

        // Manual step button for testing (optional)
        /*
        if (buttonManualStep != null) {
            buttonManualStep.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    incrementStep(); // For testing purposes
                }
            });
        }
        */
    }

    private void initStepCounter() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager == null) {
            Toast.makeText(this, "Sensor service not available!", Toast.LENGTH_LONG).show();
            isStepSensorPresent = false;
            textViewSensorStatus.setText("Sensor Unavailable: Service not found");
            return;
        }

        // Try different sensors in order of preference
        Log.d(TAG, "Checking for available step sensors...");

        // 1. Try Step Detector (most accurate for step counting)
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (stepSensor != null) {
            isStepSensorPresent = true;
            useStepCounter = false;
            useAccelerometer = false;
            Log.d(TAG, "Using STEP_DETECTOR sensor");
            textViewSensorStatus.setText("Sensor: Step Detector (Best)");
        } else {
            // 2. Try Step Counter (cumulative steps since boot)
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (stepSensor != null) {
                isStepSensorPresent = true;
                useStepCounter = true;
                useAccelerometer = false;
                isFirstStepCounterReading = true;
                Log.d(TAG, "Using STEP_COUNTER sensor");
                textViewSensorStatus.setText("Sensor: Step Counter (Good)");
            } else {
                // 3. Fallback to Accelerometer
                stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                if (stepSensor != null) {
                    isStepSensorPresent = true;
                    useStepCounter = false;
                    useAccelerometer = true;
                    Log.d(TAG, "Using ACCELEROMETER sensor for step detection");
                    textViewSensorStatus.setText("Sensor: Accelerometer (Fallback)");
                } else {
                    // No sensors available
                    isStepSensorPresent = false;
                    Log.e(TAG, "No step sensors available on this device");
                    textViewSensorStatus.setText("No sensors available - Manual mode only");
                    Toast.makeText(this, "No step sensors available. You can still manually track steps.", Toast.LENGTH_LONG).show();
                }
            }
        }

        loadStepData();
        updateStepCounterUI();
    }

    private void registerStepSensor() {
        if (sensorManager == null || stepSensor == null) {
            Log.e(TAG, "Cannot register sensor - manager or sensor is null");
            return;
        }

        int sensorDelay = useAccelerometer ? SensorManager.SENSOR_DELAY_NORMAL : SensorManager.SENSOR_DELAY_NORMAL;
        boolean registered = sensorManager.registerListener(this, stepSensor, sensorDelay);

        if (registered) {
            Log.d(TAG, "Step sensor listener registered successfully");
            String sensorType = useStepCounter ? "Step Counter" : (useAccelerometer ? "Accelerometer" : "Step Detector");
            textViewSensorStatus.setText("Sensor: " + sensorType + " - Active");
        } else {
            Log.e(TAG, "Failed to register step sensor listener");
            textViewSensorStatus.setText("Sensor: Registration Failed");
            Toast.makeText(this, "Sensor registration failed - try closing other fitness apps", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            // Direct step detection
            incrementStep();
            Log.d(TAG, "Step detected by STEP_DETECTOR");

        } else if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER && useStepCounter) {
            // Handle cumulative step counter
            int totalSteps = (int) event.values[0];

            if (isFirstStepCounterReading) {
                initialStepCount = totalSteps;
                isFirstStepCounterReading = false;
                Log.d(TAG, "Initial step counter reading: " + initialStepCount);
            } else {
                int newSteps = totalSteps - initialStepCount;
                if (newSteps > currentSteps) {
                    currentSteps = newSteps;
                    updateStepDisplay();
                    Log.d(TAG, "Step counter updated: " + currentSteps);
                }
            }

        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && useAccelerometer) {
            // Accelerometer-based step detection
            detectStepFromAccelerometer(event.values);
        }
    }

    private void detectStepFromAccelerometer(float[] values) {
        float x = values[0];
        float y = values[1];
        float z = values[2];

        // Calculate magnitude of acceleration
        lastAccelValue = currentAccelValue;
        currentAccelValue = (float) Math.sqrt(x * x + y * y + z * z);
        float delta = currentAccelValue - lastAccelValue;
        shake = shake * 0.9f + delta;

        // Detect step based on threshold and timing
        long currentTime = System.currentTimeMillis();
        if (Math.abs(shake) > STEP_THRESHOLD && !stepDetected &&
                (currentTime - lastStepTime) > STEP_DELAY_MS) {

            stepDetected = true;
            lastStepTime = currentTime;
            incrementStep();
            Log.d(TAG, "Step detected by ACCELEROMETER, shake: " + shake);

            // Reset step detection flag after a short delay
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    stepDetected = false;
                }
            }, STEP_DELAY_MS / 2);
        }
    }

    private void incrementStep() {
        currentSteps++;

        // Check for daily reset
        Calendar calendar = Calendar.getInstance();
        long currentDay = calendar.get(Calendar.DAY_OF_YEAR);

        if (currentDay != lastResetDay) {
            currentSteps = 1; // Start with the current step
            lastResetDay = currentDay;
            saveStepData();
            Toast.makeText(this, "Daily step count reset!", Toast.LENGTH_SHORT).show();
        }

        updateStepDisplay();
        saveStepData();
    }

    private void updateStepDisplay() {
        textViewStepsToday.setText(String.valueOf(currentSteps));
        updateProgressBar(currentSteps);

        String sensorType = useStepCounter ? "Counter" : (useAccelerometer ? "Accel" : "Detector");
        textViewSensorStatus.setText("Sensor: " + sensorType + " - " + currentSteps + " steps");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "Sensor accuracy changed: " + accuracy);
    }

    private void updateProgressBar(int currentSteps) {
        int stepGoal = sharedPreferences.getInt(PREF_STEP_GOAL, 0);
        if (stepGoal > 0) {
            progressBarSteps.setMax(stepGoal);
            progressBarSteps.setProgress(Math.min(currentSteps, stepGoal));
        } else {
            progressBarSteps.setMax(1);
            progressBarSteps.setProgress(0);
        }
    }

    private void updateStepCounterUI() {
        int currentSteps = sharedPreferences.getInt(PREF_CURRENT_STEPS, 0);
        int stepGoal = sharedPreferences.getInt(PREF_STEP_GOAL, 0);

        textViewStepsToday.setText(String.valueOf(currentSteps));
        textViewStepGoal.setText(String.valueOf(stepGoal));
        updateProgressBar(currentSteps);
    }

    private void saveStepData() {
        updateUserStepDataInFirestore();
    }

    private void loadStepData() {
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            Map<String, Object> userData = queryDocumentSnapshots.getDocuments().get(0).getData();
                            currentSteps = ((Long) userData.getOrDefault("currentSteps", 0L)).intValue();
                            lastResetDay = ((Long) userData.getOrDefault("lastResetDay", 0L)).longValue();
                            int stepGoal = ((Long) userData.getOrDefault("stepGoal", 0L)).intValue();

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt(PREF_CURRENT_STEPS, currentSteps);
                            editor.putLong(PREF_LAST_RESET_DAY, lastResetDay);
                            editor.putInt(PREF_STEP_GOAL, stepGoal);
                            editor.apply();

                            Log.d(TAG, "Step data loaded from Firestore: currentSteps=" + currentSteps);

                            // Check for daily reset on load
                            Calendar calendar = Calendar.getInstance();
                            long currentDay = calendar.get(Calendar.DAY_OF_YEAR);
                            if (currentDay != lastResetDay) {
                                currentSteps = 0;
                                lastResetDay = currentDay;
                                updateUserStepDataInFirestore();
                                Toast.makeText(PlannerActivity.this, "Daily step count reset!", Toast.LENGTH_SHORT).show();
                            }
                            updateStepCounterUI();
                        } else {
                            Log.d(TAG, "User document not found for step data.");
                            currentSteps = 0;
                            lastResetDay = 0;
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt(PREF_CURRENT_STEPS, 0);
                            editor.putLong(PREF_LAST_RESET_DAY, 0);
                            editor.putInt(PREF_STEP_GOAL, 0);
                            editor.apply();
                            updateStepCounterUI();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error loading step data from Firestore", e);
                        Toast.makeText(PlannerActivity.this, "Error loading step data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserStepDataInFirestore() {
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            String userId = queryDocumentSnapshots.getDocuments().get(0).getId();
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("currentSteps", currentSteps);
                            updates.put("lastResetDay", lastResetDay);
                            updates.put("stepGoal", sharedPreferences.getInt(PREF_STEP_GOAL, 0));

                            db.collection("users").document(userId)
                                    .update(updates)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "Step data updated in Firestore for user: " + username);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e(TAG, "Error updating step data in Firestore", e);
                                        }
                                    });
                        } else {
                            Log.e(TAG, "User document not found for updating step data.");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error querying user for step data update", e);
                    }
                });
    }

    private void showSetStepGoalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Daily Step Goal");
        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("e.g., 10000");
        builder.setView(input);

        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String goalStr = input.getText().toString();
                if (!goalStr.isEmpty()) {
                    int newGoal = Integer.parseInt(goalStr);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt(PREF_STEP_GOAL, newGoal);
                    editor.apply();
                    updateUserStepDataInFirestore();
                    updateStepCounterUI();
                    Toast.makeText(PlannerActivity.this, "Step goal set to " + newGoal, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PlannerActivity.this, "Please enter a valid goal", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // [Rest of the existing methods for event management remain the same]
    private void showAddEventDialog(final Map<String, Object> eventToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_event, null);
        builder.setView(dialogView);

        final EditText editTextTitle = dialogView.findViewById(R.id.editTextEventTitle);
        final EditText editTextDescription = dialogView.findViewById(R.id.editTextEventDescription);
        final TextView textViewDate = dialogView.findViewById(R.id.textViewEventDate);
        final Calendar calendar = Calendar.getInstance();

        if (eventToEdit != null) {
            builder.setTitle("Edit Event");
            editTextTitle.setText((String) eventToEdit.get("title"));
            editTextDescription.setText((String) eventToEdit.get("description"));
            com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) eventToEdit.get("date");
            if (timestamp != null) {
                calendar.setTime(timestamp.toDate());
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                textViewDate.setText(sdf.format(calendar.getTime()));
            }
        } else {
            builder.setTitle("Add New Event");
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            textViewDate.setText(sdf.format(calendar.getTime()));
        }

        textViewDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(PlannerActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(year, month, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        textViewDate.setText(sdf.format(calendar.getTime()));
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = editTextTitle.getText().toString().trim();
                String description = editTextDescription.getText().toString().trim();
                Date date = calendar.getTime();

                if (title.isEmpty()) {
                    Toast.makeText(PlannerActivity.this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (eventToEdit != null) {
                    String eventId = (String) eventToEdit.get("id");
                    updateEvent(eventId, title, description, date);
                } else {
                    addEvent(title, description, date);
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void showDeleteConfirmationDialog(final Map<String, Object> eventToDelete) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event: " + eventToDelete.get("title") + "?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String eventId = (String) eventToDelete.get("id");
                        deleteEvent(eventId);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadEvents() {
        db.collection("planner_events")
                .whereEqualTo("userId", username)
                .orderBy("date")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        eventsList.clear();
                        adapter.clear();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Map<String, Object> event = document.getData();
                            event.put("id", document.getId());
                            eventsList.add(event);

                            String title = (String) event.get("title");
                            String description = (String) event.get("description");
                            com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) event.get("date");
                            String dateStr = (timestamp != null) ? sdf.format(timestamp.toDate()) : "No Date";
                            adapter.add(dateStr + " - " + title + (description != null && !description.isEmpty() ? " (" + description + ")" : ""));
                        }
                        adapter.notifyDataSetChanged();
                        Log.d(TAG, "Events loaded successfully: " + eventsList.size());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error loading events", e);
                        Toast.makeText(PlannerActivity.this, "Error loading events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addEvent(String title, String description, Date date) {
        Map<String, Object> event = new HashMap<>();
        event.put("userId", username);
        event.put("title", title);
        event.put("description", description);
        event.put("date", new com.google.firebase.Timestamp(date));

        db.collection("planner_events")
                .add(event)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(PlannerActivity.this, "Event added successfully!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Event added with ID: " + documentReference.getId());
                        loadEvents();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PlannerActivity.this, "Error adding event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error adding event", e);
                    }
                });
    }

    private void updateEvent(String eventId, String title, String description, Date date) {
        Map<String, Object> eventUpdates = new HashMap<>();
        eventUpdates.put("title", title);
        eventUpdates.put("description", description);
        eventUpdates.put("date", new com.google.firebase.Timestamp(date));

        db.collection("planner_events").document(eventId)
                .update(eventUpdates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(PlannerActivity.this, "Event updated successfully!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Event updated with ID: " + eventId);
                        loadEvents();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PlannerActivity.this, "Error updating event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error updating event", e);
                    }
                });
    }

    private void deleteEvent(String eventId) {
        db.collection("planner_events").document(eventId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(PlannerActivity.this, "Event deleted successfully!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Event deleted with ID: " + eventId);
                        loadEvents();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PlannerActivity.this, "Error deleting event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error deleting event", e);
                    }
                });
    }
}