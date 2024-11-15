package com.example.petdoc_app;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class vaccDashActivity extends AppCompatActivity {

    private Button vaccManagerButton, viewVaccButton;
    private TextView tvUserName;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vacdash); // Make sure this matches your XML file name

        // Initialize buttons
        vaccManagerButton = findViewById(R.id.VaccManager);
        viewVaccButton = findViewById(R.id.viewVacc);
        tvUserName = findViewById(R.id.userName);

        // Retrieve userId from Intent
        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);
        String userName = intent.getStringExtra("userName");
        String loginTime = intent.getStringExtra("loginTime");

        if (userId == -1) {
            Toast.makeText(this, "User ID is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Display the user name and login time
        tvUserName.setText("Welcome, " + userName + "\nLogged in at: " + loginTime);


        // Set onClick listeners
        vaccManagerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start VaccinationManagerActivity when VaccManager button is clicked
                Intent intent = new Intent(vaccDashActivity.this, VaccManagerActivity.class);
                // Navigate to E-Vaccination Books activity
                intent.putExtra("userId", userId); // Pass userId as int
                intent.putExtra("userName", userName);   // Pass userName
                intent.putExtra("loginTime", loginTime); // Pass loginTime
                startActivity(intent);
            }
        });

        viewVaccButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start ViewVaccActivity when viewVacc button is clicked
                Intent intent = new Intent(vaccDashActivity.this, ViewVaccActivity.class);
                // Navigate to E-Vaccination Books activity
                intent.putExtra("userId", userId); // Pass userId as int
                intent.putExtra("userName", userName);   // Pass userName
                intent.putExtra("loginTime", loginTime); // Pass loginTime
                startActivity(intent);
            }
        });
    }
}
