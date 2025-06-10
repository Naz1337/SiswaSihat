package com.mad.satu_c.group_satu.siswasihat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText editTextUsername, editTextPassword;
    private Button buttonRegister;
    private TextView textViewLoginLink;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        initFirestore();
        initListeners();
    }

    /**
     * Initializes UI views by finding them by their IDs.
     */
    private void initViews() {
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLoginLink = findViewById(R.id.textViewLoginLink);
    }

    /**
     * Initializes the Firebase Firestore instance.
     */
    private void initFirestore() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Sets up click listeners for buttons and text views.
     */
    private void initListeners() {
        buttonRegister.setOnClickListener(v -> attemptRegister());
        textViewLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Close RegisterActivity
        });
    }

    /**
     * Attempts to register a new user by adding their credentials to Firestore.
     */
    private void attemptRegister() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if username already exists
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // Username already exists
                            Toast.makeText(RegisterActivity.this, "Username already exists. Please choose another.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Username is unique, proceed with registration
                            Map<String, Object> user = new HashMap<>();
                            user.put("username", username);
                            user.put("password", password); // Storing in plaintext as per project rules

                            db.collection("users")
                                    .add(user)
                                    .addOnSuccessListener(documentReference -> {
                                        Log.d(TAG, "User registered successfully with ID: " + documentReference.getId());
                                        Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish(); // Close RegisterActivity
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error registering user: ", e);
                                        Toast.makeText(RegisterActivity.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Log.e(TAG, "Error checking username existence: ", task.getException());
                        Toast.makeText(RegisterActivity.this, "Error checking username: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
