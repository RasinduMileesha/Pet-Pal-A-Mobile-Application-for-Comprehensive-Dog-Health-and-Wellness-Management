package com.example.petdoc_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class roleActivity extends AppCompatActivity {

    private ImageView ownerImageView, vetImageView;
    private TextView loginText;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.role);  // Assuming your XML is named role.xml

        // Initialize the ImageView components
        ownerImageView = findViewById(R.id.owner);
        vetImageView = findViewById(R.id.vet);
        loginText = findViewById(R.id.loginText);

        // Set click listener on the owner ImageView
        ownerImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Highlight the ImageView when clicked
                ownerImageView.setBackgroundColor(getResources().getColor(R.color.highlight_color));

                // Navigate to registerActivity.java
                Intent intent = new Intent(roleActivity.this, registerActivity.class);
                startActivity(intent);
            }
        });

        // Set click listener on the vet ImageView
        vetImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Highlight the ImageView when clicked
                vetImageView.setBackgroundColor(getResources().getColor(R.color.highlight_color));

                // Navigate to vetRegActivity.java
                Intent intent = new Intent(roleActivity.this, vetRegActivity.class);
                startActivity(intent);
            }
        });

        // Set click listener for the loginText to redirect to LoginActivity
        loginText.setOnClickListener(view -> {
            // Navigate to the LoginActivity
            Intent intent = new Intent(roleActivity.this, loginActivity.class);
            startActivity(intent);
        });
        loginText.setOnClickListener(view -> {
            // Change the text color after click
            loginText.setTextColor(getResources().getColor(android.R.color.holo_red_dark)); // Change to red or any color you want

            // Navigate to the LoginActivity
            Intent intent = new Intent(roleActivity.this, loginActivity.class);
            startActivity(intent);
        });
    }
}
