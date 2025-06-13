package com.mad.satu_c.group_satu.siswasihat;

import android.content.Intent;
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
import java.util.UUID;

public class AnonymousChatActivity extends AppCompatActivity {

    private static final String TAG = "AnonymousChatActivity";

    private FirebaseFirestore db;
    private CollectionReference chatRef;

    private RecyclerView recyclerView;
    private EditText messageInput;
    private Button sendButton;
    private Button clearChatButton; // New button for clearing chat
    private ChatAdapter chatAdapter;
    private String currentUsername; // To store the logged-in username

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anonymous_chat);

        db = FirebaseFirestore.getInstance();
        chatRef = db.collection("chat_messages");

        // Retrieve username from Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("USERNAME")) {
            currentUsername = intent.getStringExtra("USERNAME");
        } else {
            // Fallback if username is not provided (e.g., for testing)
            currentUsername = "AnonymousUser_" + UUID.randomUUID().toString().substring(0, 4);
            Log.w(TAG, "Username not provided in Intent. Using generated username: " + currentUsername);
        }

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
        clearChatButton = findViewById(R.id.buttonClearChat); // Initialize clear chat button
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

        clearChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllMessages();
            }
        });
    }

    /**
     * Sets up the RecyclerView with its adapter and layout manager.
     */
    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(currentUsername); // Pass username to adapter
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
                            ChatMessage message = dc.getDocument().toObject(ChatMessage.class);
                            // Ensure messageId is set from the document ID
                            message.setMessageId(dc.getDocument().getId());
                            switch (dc.getType()) {
                                case ADDED:
                                    chatAdapter.addMessage(message);
                                    recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1); // Scroll to bottom
                                    break;
                                case MODIFIED:
                                    chatAdapter.updateMessage(message);
                                    break;
                                case REMOVED:
                                    chatAdapter.removeMessage(message);
                                    break;
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

        String messageId = UUID.randomUUID().toString(); // Generate unique ID for the message

        Map<String, Object> message = new HashMap<>();
        message.put("messageId", messageId);
        message.put("username", currentUsername);
        message.put("message", messageText);
        message.put("timestamp", FieldValue.serverTimestamp()); // Use server timestamp

        chatRef.document(messageId).set(message) // Use messageId as document ID
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Message sent successfully!");
                    messageInput.setText(""); // Clear input field
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error sending message", e);
                    Toast.makeText(AnonymousChatActivity.this, "Failed to send message.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Clears all messages from the chat.
     */
    private void clearAllMessages() {
        chatRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (int i = 0; i < queryDocumentSnapshots.getDocuments().size(); i++) {
                queryDocumentSnapshots.getDocuments().get(i).getReference().delete()
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Document successfully deleted!"))
                        .addOnFailureListener(e -> Log.w(TAG, "Error deleting document", e));
            }
            Toast.makeText(AnonymousChatActivity.this, "Chat cleared!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error getting documents to clear chat", e);
            Toast.makeText(AnonymousChatActivity.this, "Failed to clear chat.", Toast.LENGTH_SHORT).show();
        });
    }
}
