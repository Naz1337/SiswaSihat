package com.mad.satu_c.group_satu.siswasihat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class AnonymousChatActivity extends AppCompatActivity {

    private static final String TAG = "AnonymousChatActivity";

    private FirebaseFirestore db;
    private CollectionReference chatRef;

    private RecyclerView recyclerView;
    private EditText messageInput;
    private Button sendButton;
    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anonymous_chat);

        db = FirebaseFirestore.getInstance();
        chatRef = db.collection("chat_messages");

        initViews();
        initListeners();
        setupRecyclerView();
        listenForMessages();
    }

    /**
     * Initializes UI views.
     */
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewChat);
        messageInput = findViewById(R.id.editTextMessage);
        sendButton = findViewById(R.id.buttonSend);
    }

    /**
     * Initializes listeners for UI elements.
     */
    private void initListeners() {
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    /**
     * Sets up the RecyclerView with its adapter and layout manager.
     */
    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);
    }

    /**
     * Listens for real-time updates to the chat messages collection in Firestore.
     */
    private void listenForMessages() {
        chatRef.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@NonNull QuerySnapshot snapshots,
                                        @NonNull FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "Listen failed.", e);
                            Toast.makeText(AnonymousChatActivity.this, "Error loading messages.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                ChatMessage message = dc.getDocument().toObject(ChatMessage.class);
                                chatAdapter.addMessage(message);
                                recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1); // Scroll to bottom
                            }
                        }
                    }
                });
    }

    /**
     * Sends a new message to the Firestore database.
     */
    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (messageText.isEmpty()) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> message = new HashMap<>();
        message.put("message", messageText);
        message.put("timestamp", FieldValue.serverTimestamp()); // Use server timestamp

        chatRef.add(message)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Message sent successfully!");
                    messageInput.setText(""); // Clear input field
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error sending message", e);
                    Toast.makeText(AnonymousChatActivity.this, "Failed to send message.", Toast.LENGTH_SHORT).show();
                });
    }
}
