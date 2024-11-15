package com.example.petdoc_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class petManagerActivity extends AppCompatActivity {

    private ImageView logo;
    private TextView tvUserName;
    private Button petRegisterButton, delPet;
    private int userId; // User ID as an integer

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.petmanager);

        // Binding UI elements to Java variables
        logo = findViewById(R.id.logo);
        // Initialize TextView for userName
        tvUserName = findViewById(R.id.userName);
        petRegisterButton = findViewById(R.id.PetRegister);
        delPet = findViewById(R.id.delPet);

        // Retrieve userId safely from the Intent as an int
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

        // Set up button listeners
        petRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to go to Register Pet Activity
                Intent intent = new Intent(petManagerActivity.this, petRegisterActivity.class);
                intent.putExtra("userId", userId); // Pass userId as int
                startActivity(intent);
            }
        });

        //  delPet button if needed
        // Set up button listeners
        delPet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to go to Register Pet Activity
                Intent intent = new Intent(petManagerActivity.this, crudPetActivity.class);
                intent.putExtra("userId", userId); // Pass userId as int
                startActivity(intent);
            }
        });
    }
}
