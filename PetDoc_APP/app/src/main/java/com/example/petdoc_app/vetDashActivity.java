package com.example.petdoc_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class vetDashActivity extends AppCompatActivity {

    private Button report, viewEvaccination, communityChat;
    private TextView tvVetName;
    private ImageButton imageButton4;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vetdash);

        viewEvaccination = findViewById(R.id.viewEvaccination);
        communityChat = findViewById(R.id.communityChat);
        report = findViewById(R.id.viewreport);
        tvVetName = findViewById(R.id.userName);
        imageButton4 = findViewById(R.id.imageButton4);

        // Retrieve userId, userName, and loginTime consistently
        int userId = getIntent().getIntExtra("userId", -1);
        String name = getIntent().getStringExtra("userName");
        String loginTime = getIntent().getStringExtra("loginTime");

        tvVetName.setText("Welcome Dr. " + name + "\nLogged in at: " + loginTime);

        communityChat.setOnClickListener(v -> {
            Intent intent = new Intent(vetDashActivity.this, ChatActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("userName", name);
            intent.putExtra("userType", "Vet");  // Pass userType as Vet
            startActivity(intent);
        });

        viewEvaccination.setOnClickListener(v -> {
            Intent intent = new Intent(vetDashActivity.this, VETVaccActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("userName", name);
            intent.putExtra("loginTime", loginTime);
            startActivity(intent);
        });

        report.setOnClickListener(v -> {
            Intent intent = new Intent(vetDashActivity.this, DiseaseReportActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("userName", name);
            intent.putExtra("loginTime", loginTime);
            startActivity(intent);
        });

        imageButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start LoginActivity
                Intent intent = new Intent(vetDashActivity.this, loginActivity.class);
                startActivity(intent);

                // Optionally, finish the current activity to prevent going back to it
                finish();
            }
        });
    }
}
