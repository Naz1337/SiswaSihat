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

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegisterLink;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegisterLink = findViewById(R.id.textViewRegisterLink);
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
        buttonLogin.setOnClickListener(v -> attemptLogin());
        textViewRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Attempts to log in the user by checking credentials against Firestore.
     */
    private void attemptLogin() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .whereEqualTo("username", username)
                .whereEqualTo("password", password)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // Successful login
                            Log.d(TAG, "Login successful for user: " + username);
                            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                            intent.putExtra("USERNAME", username); // Pass username to DashboardActivity
                            startActivity(intent);
                            finish(); // Close LoginActivity
                        } else {
                            // No matching user found
                            Log.d(TAG, "Login failed: Invalid credentials for user: " + username);
                            Toast.makeText(LoginActivity.this, R.string.login_failed, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Error querying Firestore
                        Log.e(TAG, "Error querying Firestore for login: ", task.getException());
                        Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
