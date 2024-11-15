package com.example.petdoc_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class loginActivity extends AppCompatActivity {
    private EditText etGmail, etPassword;
    private Button btnLogin;
    private TextView loginText, loginText2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        etGmail = findViewById(R.id.etGmail);
        etPassword = findViewById(R.id.password);
        btnLogin = findViewById(R.id.btnlogin);
        loginText = findViewById(R.id.loginText);
        loginText2 = findViewById(R.id.loginText2);

        // Redirect to registerActivity
        loginText.setOnClickListener(view -> {
            loginText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            startActivity(new Intent(loginActivity.this, registerActivity.class));
        });

        // Redirect to vetRegActivity
        loginText2.setOnClickListener(view -> {
            loginText2.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            startActivity(new Intent(loginActivity.this, vetRegActivity.class));
        });

        // Set login button click listener
        btnLogin.setOnClickListener(view -> validateAndLogin());
    }

    private void validateAndLogin() {
        String gmail = etGmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!isValidEmail(gmail)) {
            etGmail.setError("Please enter a valid email address.");
            etGmail.requestFocus();
        } else if (!isValidPassword(password)) {
            etPassword.setError("Password must be at least 6 characters long.");
            etPassword.requestFocus();
        } else {
            login(gmail, password);
        }
    }

    // Email validation using regular expression
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Password validation with minimum length requirement
    private boolean isValidPassword(String password) {
        return password.length() >= 6;
    }

    private void login(String gmail, String password) {
        try (Connection connection = new ConnectionHelper().conclass()) {
            if (connection == null) {
                Toast.makeText(this, "Failed to connect to the database.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Attempt login for PetOwner
            String queryPetOwner = "SELECT Id, Name FROM PetOwner WHERE Gmail = ? AND Password = ?";
            if (checkCredentials(connection, queryPetOwner, gmail, password, "PetOwner")) {
                return;
            }

            // Attempt login for Vets
            String queryVet = "SELECT VetId, Name FROM Vets WHERE Gmail = ? AND Password = ?";
            if (checkCredentials(connection, queryVet, gmail, password, "Vet")) {
                return;
            }

            // If no matching credentials found in either table
            Toast.makeText(this, "Invalid credentials. Please try again.", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e("Login Error", e.getMessage());
            Toast.makeText(this, "An error occurred. Please try again later.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkCredentials(Connection connection, String query, String gmail, String password, String userType) {
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, gmail);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = userType.equals("Vet") ? rs.getInt("VetId") : rs.getInt("Id");
                    String name = rs.getString("Name");
                    navigateToDashboard(userType, id, name);
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e("Login Error", e.getMessage());
        }
        return false;
    }

    private void navigateToDashboard(String userType, int id, String name) {
        String loginTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
        Log.d("Navigation", "Redirecting to dashboard. UserType: " + userType);

        Intent intent = new Intent(this, userType.equals("Vet") ? vetDashActivity.class : userDashActivity.class);
        intent.putExtra("userId", id);
        intent.putExtra("userName", name);
        intent.putExtra("userType", userType);
        intent.putExtra("loginTime", loginTime);

        try {
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e("Navigation Error", e.getMessage());
        }
    }
}
