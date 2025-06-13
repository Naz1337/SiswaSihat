package com.mad.satu_c.group_satu.siswasihat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> chatMessages;
    private String currentUsername;
    private Context context; // Added context for AlertDialog

    public ChatAdapter(String currentUsername) {
        this.chatMessages = new ArrayList<>();
        this.currentUsername = currentUsername;
    }

    public void addMessage(ChatMessage message) {
        chatMessages.add(message);
        notifyItemInserted(chatMessages.size() - 1);
    }

    public void updateMessage(ChatMessage message) {
        for (int i = 0; i < chatMessages.size(); i++) {
            if (chatMessages.get(i).getMessageId().equals(message.getMessageId())) {
                chatMessages.set(i, message);
                notifyItemChanged(i);
                return;
            }
        }
    }

    public void removeMessage(ChatMessage message) {
        for (int i = 0; i < chatMessages.size(); i++) {
            if (chatMessages.get(i).getMessageId().equals(message.getMessageId())) {
                chatMessages.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext(); // Get context here
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);
        holder.usernameTextView.setText(message.getUsername());
        holder.messageTextView.setText(message.getMessage());

        if (message.getTimestamp() != null) {
            Date date = message.getTimestamp().toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            holder.timestampTextView.setText(sdf.format(date));
        } else {
            holder.timestampTextView.setText("");
        }

        // Show/hide edit button based on username
        if (message.getUsername() != null && message.getUsername().equals(currentUsername)) {
            holder.editButton.setVisibility(View.VISIBLE);
            holder.editButton.setOnClickListener(v -> showEditDialog(message));
        } else {
            holder.editButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    private void showEditDialog(ChatMessage message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Message");

        final EditText input = new EditText(context);
        input.setText(message.getMessage());
        builder.setView(input);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newMessageText = input.getText().toString().trim();
                if (!newMessageText.isEmpty()) {
                    FirebaseFirestore.getInstance().collection("chat_messages")
                            .document(message.getMessageId())
                            .update("message", newMessageText)
                            .addOnSuccessListener(aVoid -> Toast.makeText(context, "Message updated", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(context, "Error updating message", Toast.LENGTH_SHORT).show());
                }
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        TextView messageTextView;
        TextView timestampTextView;
        Button editButton; // New edit button

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
            editButton = itemView.findViewById(R.id.buttonEditMessage);
        }
    }
}

