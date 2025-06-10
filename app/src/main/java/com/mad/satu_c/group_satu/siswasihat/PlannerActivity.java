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
    private long lastResetDay = 0; // Store the day of the year when steps were last reset

    private TextView textViewStepsToday;
    private TextView textViewStepGoal;
    private ProgressBar progressBarSteps;
    private Button buttonSetStepGoal;
    private TextView textViewSensorStatus;

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
            finish(); // Close activity if no username
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

        // Initialize Step Counter
        initStepCounter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isStepSensorPresent) {
            if (sensorManager == null) {
                Log.e(TAG, "SensorManager is null in onResume(), re-initializing.");
                sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            }

            if (sensorManager != null) {
                // Re-check sensor availability at registration time
                Sensor currentSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
                if (currentSensor == null) {
                    Log.e(TAG, "Step detector sensor no longer available in onResume().");
                    isStepSensorPresent = false; // Update flag if sensor disappeared
                    textViewSensorStatus.setText("Sensor Unavailable: Sensor disappeared");
                } else {
                    Log.d(TAG, "Attempting to register step detector listener with delay: " + SensorManager.SENSOR_DELAY_NORMAL);
                    boolean registered = sensorManager.registerListener(this, currentSensor, SensorManager.SENSOR_DELAY_NORMAL);
                    if (registered) {
                        Log.d(TAG, "Step detector listener registered successfully in onResume().");
                        textViewSensorStatus.setText("Sensor Status: Active");
                    } else {
                        Log.e(TAG, "Failed to register step detector listener in onResume(). Possible reasons: " +
                                "1. Sensor already registered (unlikely if onPause() works), " +
                                "2. Insufficient permissions (unlikely for step detector), " +
                                "3. Sensor temporarily unavailable or busy.");
                        textViewSensorStatus.setText("Sensor Status: Busy - Close other fitness apps");
                        Toast.makeText(this, "Sensor busy - check other apps", Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                Log.e(TAG, "Sensor service unavailable after re-initialization in onResume().");
                textViewSensorStatus.setText("Sensor Unavailable: Service not found");
            }
        }
        loadStepData(); // Load step data when activity resumes
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isStepSensorPresent) {
            sensorManager.unregisterListener(this);
            Log.d(TAG, "Step detector listener unregistered in onPause()");
        }
        saveStepData(); // Save step data when activity pauses
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
    }

    private void initListeners() {
        buttonAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddEventDialog(null); // Pass null for adding new event
            }
        });

        listViewEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> event = eventsList.get(position);
                showAddEventDialog(event); // Pass event for editing
            }
        });

        listViewEvents.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> event = eventsList.get(position);
                showDeleteConfirmationDialog(event);
                return true; // Consume the long click
            }
        });

        buttonSetStepGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSetStepGoalDialog();
            }
        });
    }

    private void initStepCounter() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            Log.d(TAG, "Checking for step detector sensor...");
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            if (stepSensor == null) {
                Toast.makeText(this, "Step Detector Sensor not available!", Toast.LENGTH_LONG).show();
                isStepSensorPresent = false;
                Log.e(TAG, "Step detector sensor not available on this device");
                textViewSensorStatus.setText("Sensor Unavailable: Device lacks step detector");
            } else {
                isStepSensorPresent = true;
                Log.d(TAG, "Step detector sensor available");
                textViewSensorStatus.setText("Sensor Status: Ready");
            }
        } else {
            Toast.makeText(this, "Sensor service not available!", Toast.LENGTH_LONG).show();
            isStepSensorPresent = false;
            Log.e(TAG, "Sensor service not available!");
            textViewSensorStatus.setText("Sensor Unavailable: Service not found");
        }
        loadStepData(); // Load initial step data
        updateStepCounterUI();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, "Sensor event received: " + event.sensor.getName() + " Type: " + event.sensor.getType());
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            currentSteps++; // Increment for each detected step

            Log.d(TAG, "Step detected. Current steps: " + currentSteps);

            // Check for daily reset
            Calendar calendar = Calendar.getInstance();
            long currentDay = calendar.get(Calendar.DAY_OF_YEAR);

            if (currentDay != lastResetDay) {
                // New day, reset steps
                currentSteps = 0;
                lastResetDay = currentDay;
                saveStepData(); // Save new reset day
                Toast.makeText(this, "Daily step count reset!", Toast.LENGTH_SHORT).show();
            }

            textViewStepsToday.setText(String.valueOf(currentSteps));
            updateProgressBar(currentSteps);
            saveStepData(); // Save steps after each increment
            textViewSensorStatus.setText("Sensor Status: Active - " + currentSteps + " steps");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used for step counter
    }

    private void updateProgressBar(int currentSteps) {
        int stepGoal = sharedPreferences.getInt(PREF_STEP_GOAL, 0);
        if (stepGoal > 0) {
            progressBarSteps.setMax(stepGoal);
            progressBarSteps.setProgress(Math.min(currentSteps, stepGoal));
        } else {
            progressBarSteps.setMax(1); // Avoid division by zero if goal is 0
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
        // This method will now trigger a Firestore update
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

                            // Update SharedPreferences for local access (optional, but good for consistency)
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt(PREF_CURRENT_STEPS, currentSteps);
                            editor.putLong(PREF_LAST_RESET_DAY, lastResetDay);
                            editor.putInt(PREF_STEP_GOAL, stepGoal);
                            editor.apply();

                            Log.d(TAG, "Step data loaded from Firestore: currentSteps=" + currentSteps + ", lastResetDay=" + lastResetDay + ", stepGoal=" + stepGoal);

                            // Check for daily reset on load as well
                            Calendar calendar = Calendar.getInstance();
                            long currentDay = calendar.get(Calendar.DAY_OF_YEAR);
                            if (currentDay != lastResetDay) {
                                currentSteps = 0; // Reset steps for the new day
                                lastResetDay = currentDay;
                                updateUserStepDataInFirestore(); // Save new reset day to Firestore
                                Toast.makeText(PlannerActivity.this, "Daily step count reset!", Toast.LENGTH_SHORT).show();
                            }
                            updateStepCounterUI();
                        } else {
                            Log.d(TAG, "User document not found for step data.");
                            // Initialize with default values if user data not found
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
                            updates.put("stepGoal", sharedPreferences.getInt(PREF_STEP_GOAL, 0)); // Get goal from SharedPreferences for now

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
                    editor.apply(); // Save to SharedPreferences first
                    updateUserStepDataInFirestore(); // Then update Firestore
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
            // Populate fields for editing
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
            textViewDate.setText(sdf.format(calendar.getTime())); // Set current date as default
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
                    // Update existing event
                    String eventId = (String) eventToEdit.get("id"); // Assuming 'id' is stored in the map
                    updateEvent(eventId, title, description, date);
                } else {
                    // Add new event
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


    /**
     * Loads planner events from Firestore for the current user.
     */
    private void loadEvents() {
        db.collection("planner_events")
                .whereEqualTo("userId", username)
                .orderBy("date") // Order by date for better display
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        eventsList.clear();
                        adapter.clear();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Map<String, Object> event = document.getData();
                            event.put("id", document.getId()); // Store document ID for update/delete
                            eventsList.add(event);

                            // Display event in ListView
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

    /**
     * Adds a new event to Firestore.
     * @param title The title of the event.
     * @param description The description of the event.
     * @param date The date of the event.
     */
    private void addEvent(String title, String description, Date date) {
        Map<String, Object> event = new HashMap<>();
        event.put("userId", username);
        event.put("title", title);
        event.put("description", description);
        event.put("date", new com.google.firebase.Timestamp(date)); // Use Firebase Timestamp

        db.collection("planner_events")
                .add(event)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(PlannerActivity.this, "Event added successfully!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Event added with ID: " + documentReference.getId());
                        loadEvents(); // Reload events to update UI
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

    /**
     * Updates an existing event in Firestore.
     * @param eventId The ID of the event to update.
     * @param title The new title of the event.
     * @param description The new description of the event.
     * @param date The new date of the event.
     */
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
                        loadEvents(); // Reload events to update UI
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

    /**
     * Deletes an event from Firestore.
     * @param eventId The ID of the event to delete.
     */
    private void deleteEvent(String eventId) {
        db.collection("planner_events").document(eventId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(PlannerActivity.this, "Event deleted successfully!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Event deleted with ID: " + eventId);
                        loadEvents(); // Reload events to update UI
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
