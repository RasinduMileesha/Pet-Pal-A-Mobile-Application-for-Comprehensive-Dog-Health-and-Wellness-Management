package com.example.petdoc_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class userDashActivity extends AppCompatActivity {

    private Button btnDiseaseDetection, btnSearchVetCenter, btnEvaccination, btnCommunityChat, btnpetmanager;
    private TextView tvUserName;
    private ImageButton imageButton4;
    private int userId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userdash);

        // Initialize buttons
        btnDiseaseDetection = findViewById(R.id.DiseaseDetection);
        btnSearchVetCenter = findViewById(R.id.vetcenter);
        btnEvaccination = findViewById(R.id.Evaccination);
        btnCommunityChat = findViewById(R.id.communityChat);
        btnpetmanager = findViewById(R.id.petregister);
        imageButton4 = findViewById(R.id.imageButton4);




        // Initialize TextView for userName
        tvUserName = findViewById(R.id.userName);


        // Retrieve userId, userName, and loginTime safely from the Intent
        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1); // Use -1 as a default value for error checking
        String userName = intent.getStringExtra("userName");
        String loginTime = intent.getStringExtra("loginTime");

        // Validate userId
        if (userId == -1) {
            Toast.makeText(this, "User ID is missing", Toast.LENGTH_SHORT).show();
            finish(); // End the activity if userId is not available
            return;
        }



        // Display the user name and login time
        tvUserName.setText("Welcome, " + userName + "\nLogged in at: " + loginTime);

        // Set button click listeners
        btnDiseaseDetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to Skin Disease Detection activity
                Intent intent = new Intent(userDashActivity.this, DiseaseDetectActivity.class);
                intent.putExtra("userId", userId);       // Pass userId as int
                intent.putExtra("userName", userName);   // Pass userName
                intent.putExtra("loginTime", loginTime); // Pass loginTime
                startActivity(intent);
            }
        });


        // Set button click listener for petManager
        btnpetmanager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to petManagerActivity and pass userId, userName, and loginTime
                Intent intent = new Intent(userDashActivity.this, petManagerActivity.class);
                intent.putExtra("userId", userId);       // Pass userId as int
                intent.putExtra("userName", userName);   // Pass userName
                intent.putExtra("loginTime", loginTime); // Pass loginTime
                startActivity(intent);
            }
        });

        // Set button click listener for community chat
        btnCommunityChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to Community Chat activity
                Intent intent = new Intent(userDashActivity.this, ChatActivity.class);
                intent.putExtra("userId", userId); // Pass userId as int
                intent.putExtra("userName", userName);   // Pass userName
                intent.putExtra("userType", "PetOwner");  // Pass userType as PetOwner
                startActivity(intent);
            }
        });

        btnEvaccination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to E-Vaccination Books activity
                Intent intent = new Intent(userDashActivity.this, vaccDashActivity.class);
                intent.putExtra("userId", userId); // Pass userId as int
                intent.putExtra("userName", userName);   // Pass userName
                intent.putExtra("loginTime", loginTime); // Pass loginTime
                startActivity(intent);
            }
        });

        btnSearchVetCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to Search Vet Centers activity
                Intent intent = new Intent(userDashActivity.this, vetcenterActivity.class);
                startActivity(intent);
            }
        });

        imageButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start LoginActivity
                Intent intent = new Intent(userDashActivity.this, loginActivity.class);
                startActivity(intent);

                // Optionally, finish the current activity to prevent going back to it
                finish();
            }
        });




        // Uncomment other button click listeners as needed
        /*
        // Set button click listeners
        btnDiseaseDetection.setOnClickListener(new View.OnClickListener() {
           @Override
            public void onClick(View view) {
                // Navigate to Skin Disease Detection activity
                Intent intent = new Intent(userDashActivity.this, DiseaseDetectionActivity.class);
                startActivity(intent);
            }
        });

        btnSearchPetOwner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to Search Vet Centers activity
                Intent intent = new Intent(userDashActivity.this, SearchVetCentersActivity.class);
                startActivity(intent);
            }
        });

        btnEvaccination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to E-Vaccination Books activity
                Intent intent = new Intent(userDashActivity.this, EVaccinationBooksActivity.class);
                startActivity(intent);
            }
        });

        btnCommunityChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to Community Chat activity
                Intent intent = new Intent(userDashActivity.this, CommunityChatActivity.class);
                startActivity(intent);
            }
        });
        */
    }
}
