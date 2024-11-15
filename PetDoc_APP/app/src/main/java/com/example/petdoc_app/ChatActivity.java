package com.example.petdoc_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerViewChat;
    private EditText editTextMessage;
    private Button buttonSend;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private int userId;
    private String userName;
    private String userType;
    private ConnectionHelper connectionHelper;
    private Handler handler;
    private Runnable chatRefreshRunnable;

    private static final int REFRESH_INTERVAL = 3000; // Refresh every 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        initializeViews();
        connectionHelper = new ConnectionHelper();

        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);
        userName = intent.getStringExtra("userName");
        userType = intent.getStringExtra("userType");

        if (userId == -1) {
            Toast.makeText(this, "User ID is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        buttonSend.setOnClickListener(v -> {
            String message = editTextMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                new SendMessageTask().execute(message);
                editTextMessage.setText("");
            } else {
                Toast.makeText(ChatActivity.this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize the Handler and Runnable for auto-refresh
        handler = new Handler();
        chatRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                new LoadChatMessagesTask().execute();
                handler.postDelayed(this, REFRESH_INTERVAL);
            }
        };

        // Start initial load of messages
        new LoadChatMessagesTask().execute();
    }

    private void initializeViews() {
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(chatAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start auto-refresh when the activity is resumed
        handler.post(chatRefreshRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop auto-refresh when the activity is paused
        handler.removeCallbacks(chatRefreshRunnable);
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadChatMessagesTask extends AsyncTask<Void, Void, List<ChatMessage>> {
        @Override
        protected List<ChatMessage> doInBackground(Void... voids) {
            List<ChatMessage> messages = new ArrayList<>();
            try (Connection connection = connectionHelper.conclass()) {
                if (connection != null) {
                    String query = "SELECT UserName, MessageText, UserType FROM Chat ORDER BY Timestamp ASC";
                    PreparedStatement stmt = connection.prepareStatement(query);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        String userName = rs.getString("UserName");
                        String messageText = rs.getString("MessageText");
                        String userType = rs.getString("UserType");

                        messages.add(new ChatMessage(userName, messageText, userType));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return messages;
        }

        @Override
        protected void onPostExecute(List<ChatMessage> messages) {
            chatMessages.clear();
            chatMessages.addAll(messages);
            chatAdapter.notifyDataSetChanged();
            recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class SendMessageTask extends AsyncTask<String, Void, Boolean> {
        private String message;

        @Override
        protected Boolean doInBackground(String... params) {
            message = params[0];
            boolean success = false;
            try (Connection connection = connectionHelper.conclass()) {
                if (connection != null) {
                    String query = "INSERT INTO Chat (UserID, MessageText, UserType, UserName) VALUES (?, ?, ?, ?)";
                    PreparedStatement pstmt = connection.prepareStatement(query);
                    pstmt.setInt(1, userId);
                    pstmt.setString(2, message);
                    pstmt.setString(3, userType);
                    pstmt.setString(4, userName);

                    pstmt.executeUpdate();
                    success = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                chatMessages.add(new ChatMessage(userName, message, userType));
                chatAdapter.notifyDataSetChanged();
                recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
            } else {
                Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
