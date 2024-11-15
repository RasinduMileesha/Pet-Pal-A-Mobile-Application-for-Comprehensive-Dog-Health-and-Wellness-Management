package com.example.petdoc_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class splashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // Duration of the splash screen in milliseconds
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash); // Make sure your layout file is named "activity_splash.xml"

        progressBar = findViewById(R.id.progressBar); // Initialize the progress bar

        // Start a Handler to move to the next activity after a delay
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(splashActivity.this, roleActivity.class);
            startActivity(intent);
            finish(); // Close the splash activity so it won't come back when the user presses back
        }, SPLASH_DURATION);

        // Start loading progress
        loadProgressBar();
    }

    private void loadProgressBar() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            int progressStatus = 0;

            @Override
            public void run() {
                if (progressStatus < 100) {
                    progressStatus += 2; // Update progress incrementally
                    progressBar.setProgress(progressStatus);
                    handler.postDelayed(this, 60); // Update progress every 60 milliseconds
                }
            }
        }, 60);
    }
}
