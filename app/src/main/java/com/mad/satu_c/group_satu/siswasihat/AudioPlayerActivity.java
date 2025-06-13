package com.mad.satu_c.group_satu.siswasihat;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AudioPlayerActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> titleList; // stores titles
    private Map<String, String> linkMap; // title -> YouTube link
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);
        db = FirebaseFirestore.getInstance();
        initViews();
        initListeners();
        loadYouTubeLinks();
    }

    private void initViews() {
        listView = findViewById(R.id.listView);
        Button btnAddLink = findViewById(R.id.btnAddAudio);
        titleList = new ArrayList<>();
        linkMap = new HashMap<>();
        adapter = new ArrayAdapter<String>(this, R.layout.list_item_audio, R.id.tvAudioName, titleList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                Button btnPlay = view.findViewById(R.id.btnPlay);
                Button btnEdit = view.findViewById(R.id.btnEdit);
                Button btnDelete = view.findViewById(R.id.btnDelete);
                
                btnPlay.setOnClickListener(v -> openYouTubeLink(position));
                btnEdit.setOnClickListener(v -> editTitleAndLink(position));
                btnDelete.setOnClickListener(v -> deleteYouTubeLink(position));
                return view;
            }
        };
        listView.setAdapter(adapter);
    }

    private void initListeners() {
        findViewById(R.id.btnAddAudio).setOnClickListener(v -> {
            showAddLinkDialog();
        });
    }

    private void showAddLinkDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_audio_name, null);
        EditText editTextTitle = dialogView.findViewById(R.id.editTextAudioName);
        EditText editTextLink = new EditText(this);
        editTextLink.setHint("Enter YouTube Link");
        ((ViewGroup) dialogView).addView(editTextLink);

        builder.setView(dialogView)
                .setTitle("Add YouTube Link")
                .setPositiveButton("Save", (dialog, which) -> {
                    String title = editTextTitle.getText().toString();
                    String link = editTextLink.getText().toString();
                    if (!title.isEmpty() && !link.isEmpty()) {
                        saveYouTubeLink(title, link);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void saveYouTubeLink(String title, String link) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("link", link);
        db.collection("youtube_links").add(data)
                .addOnSuccessListener(documentReference -> {
                    titleList.add(title);
                    linkMap.put(title, link);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Link saved", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving link", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadYouTubeLinks() {
        db.collection("youtube_links").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    titleList.clear();
                    linkMap.clear();
                    queryDocumentSnapshots.forEach(doc -> {
                        String title = doc.getString("title");
                        String link = doc.getString("link");
                        titleList.add(title);
                        linkMap.put(title, link);
                    });
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading links", Toast.LENGTH_SHORT).show();
                });
    }

    private void openYouTubeLink(int position) {
        String title = titleList.get(position);
        String link = linkMap.get(title);
        if (link != null) {
            // Use implicit intent to open YouTube
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(link));
            // Check if YouTube app is installed
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // If YouTube app is not installed, open in browser
                intent.setData(Uri.parse("https://www.youtube.com/watch?v=" + extractVideoId(link)));
                startActivity(intent);
            }
        }
    }

    private String extractVideoId(String youtubeUrl) {
        String videoId = null;
        if (youtubeUrl != null && youtubeUrl.trim().length() > 0) {
            if (youtubeUrl.contains("youtu.be/")) {
                videoId = youtubeUrl.substring(youtubeUrl.lastIndexOf("/") + 1);
            } else if (youtubeUrl.contains("youtube.com/watch?v=")) {
                videoId = youtubeUrl.substring(youtubeUrl.indexOf("v=") + 2);
                if (videoId.contains("&")) {
                    videoId = videoId.substring(0, videoId.indexOf("&"));
                }
            }
        }
        return videoId;
    }

    private void deleteYouTubeLink(int position) {
        String title = titleList.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Link")
                .setMessage("Are you sure you want to delete this link?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Delete from Firestore
                    db.collection("youtube_links")
                            .whereEqualTo("title", title)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    queryDocumentSnapshots.getDocuments().get(0).getReference()
                                            .delete()
                                            .addOnSuccessListener(aVoid -> {
                                                // Remove from local lists
                                                titleList.remove(position);
                                                linkMap.remove(title);
                                                adapter.notifyDataSetChanged();
                                                Toast.makeText(AudioPlayerActivity.this, 
                                                    "Link deleted successfully", 
                                                    Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(AudioPlayerActivity.this,
                                                    "Error deleting link",
                                                    Toast.LENGTH_SHORT).show();
                                            });
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void editTitleAndLink(int position) {
        String currentTitle = titleList.get(position);
        String currentLink = linkMap.get(currentTitle);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_audio_name, null);
        EditText editTextTitle = dialogView.findViewById(R.id.editTextAudioName);
        EditText editTextLink = new EditText(this);
        editTextLink.setHint("Enter YouTube Link");
        editTextTitle.setText(currentTitle);
        editTextLink.setText(currentLink);
        ((ViewGroup) dialogView).addView(editTextLink);

        builder.setView(dialogView)
                .setTitle("Edit Title and Link")
                .setPositiveButton("Save", (dialog, which) -> {
                    String newTitle = editTextTitle.getText().toString();
                    String newLink = editTextLink.getText().toString();
                    if (!newTitle.isEmpty() && !newLink.isEmpty()) {
                        updateTitleAndLink(currentTitle, newTitle, newLink);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void updateTitleAndLink(String oldTitle, String newTitle, String newLink) {
        db.collection("youtube_links").whereEqualTo("title", oldTitle).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentReference docRef = queryDocumentSnapshots.getDocuments().get(0).getReference();
                        docRef.update("title", newTitle, "link", newLink)
                                .addOnSuccessListener(aVoid -> {
                                    titleList.set(titleList.indexOf(oldTitle), newTitle);
                                    linkMap.remove(oldTitle);
                                    linkMap.put(newTitle, newLink);
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(this, "Title and link updated", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error updating title and link", Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }
}
