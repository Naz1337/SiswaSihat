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
import java.util.Map;

public class PlannerActivity extends AppCompatActivity {
    private static final String TAG = "PlannerActivity";
    private String username;
    private FirebaseFirestore db;
    private List<Map<String, Object>> eventsList;
    private ArrayAdapter<String> adapter;
    private ListView listViewEvents;
    private Button buttonAddEvent;

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

        // Initialize views and adapter
        initViews();
        initListeners();

        // Load events
        loadEvents();
    }

    private void initViews() {
        listViewEvents = findViewById(R.id.listViewEvents);
        buttonAddEvent = findViewById(R.id.buttonAddEvent);
        eventsList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        listViewEvents.setAdapter(adapter);
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
