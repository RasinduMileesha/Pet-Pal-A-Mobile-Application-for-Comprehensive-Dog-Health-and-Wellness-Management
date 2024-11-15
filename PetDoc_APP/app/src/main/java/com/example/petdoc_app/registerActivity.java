package com.example.petdoc_app;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class registerActivity extends AppCompatActivity {

    private EditText etID, etName, etGmail, etMobile, etAddress, etCityProvince, etPassword, etRegistrationDate;
    private Button btnRegister;
    private TextView loginText;
    private ConnectionHelper connectionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        // Initialize UI components
        initializeViews();
        connectionHelper = new ConnectionHelper();  // Initialize ConnectionHelper

        // Set registration date to the current date
        setRegistrationDate();

        // Register button listener
        btnRegister.setOnClickListener(view -> registerUser());

        // Login redirection listener
        loginText.setOnClickListener(view -> redirectToLogin());
    }

    private void initializeViews() {
        etID = findViewById(R.id.etID);
        etName = findViewById(R.id.etName);
        etGmail = findViewById(R.id.etGmail);
        etMobile = findViewById(R.id.etMobile);
        etAddress = findViewById(R.id.etAddress);
        etCityProvince = findViewById(R.id.etCityProvince);
        etPassword = findViewById(R.id.password);
        etRegistrationDate = findViewById(R.id.etRegistrationDate);
        btnRegister = findViewById(R.id.btnRegister);
        loginText = findViewById(R.id.loginText);
    }

    private void setRegistrationDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        etRegistrationDate.setText(currentDate);
        etRegistrationDate.setEnabled(false);
        etRegistrationDate.setTextColor(getResources().getColor(android.R.color.black));
        etRegistrationDate.setTextSize(18);
        etRegistrationDate.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
        etRegistrationDate.setGravity(android.view.Gravity.CENTER);
    }

    private void registerUser() {
        // Get user input
        String nicPassport = etID.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String gmail = etGmail.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String cityProvince = etCityProvince.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String registrationDate = etRegistrationDate.getText().toString();

        // Validate each input field
        if (!isNicValid(nicPassport)) {
            Toast.makeText(this, "NIC must be 10 digits followed by 'X' or 'V'", Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.isEmpty()) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isEmailValid(gmail)) {
            Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isMobileValid(mobile)) {
            Toast.makeText(this, "Mobile number must be 10 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        if (address.isEmpty()) {
            Toast.makeText(this, "Address is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cityProvince.isEmpty()) {
            Toast.makeText(this, "City/Province is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Perform the database registration if all validations pass
        if (insertUserIntoDatabase(nicPassport, name, gmail, mobile, address, cityProvince, password, registrationDate)) {
            Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        } else {
            Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show();
        }
    }

    // Validation for NIC
    private boolean isNicValid(String nic) {
        // Check that NIC is exactly 10 characters, digits followed by 'X' or 'V'
        return nic.matches("^\\d{9}[VXvx]$");
    }

    // Validation for email
    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Validation for mobile
    private boolean isMobileValid(String mobile) {
        // Check that mobile is exactly 10 digits
        return mobile.matches("^\\d{10}$");
    }



    private boolean insertUserIntoDatabase(String nicPassport, String name, String gmail, String mobile, String address, String cityProvince, String password, String registrationDate) {
        Connection connection = connectionHelper.conclass();  // Get the connection

        if (connection == null) {
            Toast.makeText(this, "Database connection error", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Remove 'ID' from the INSERT statement as it might be auto-generated
        String insertQuery = "INSERT INTO PetOwner (NICPassport, Name, Gmail, Mobile, Address, CityProvince, Password, RegistrationDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            // Set parameters for the prepared statement
            preparedStatement.setString(1, nicPassport);  // Set NIC/Passport as the first parameter
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, gmail);
            preparedStatement.setString(4, mobile);
            preparedStatement.setString(5, address);
            preparedStatement.setString(6, cityProvince);
            preparedStatement.setString(7, password);
            preparedStatement.setString(8, registrationDate);

            // Execute the insert query
            preparedStatement.executeUpdate();
            return true;  // Registration successful
        } catch (SQLException e) {
            e.printStackTrace();
            return false;  // Registration failed
        }
    }



    private void redirectToLogin() {
        loginText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        Intent intent = new Intent(registerActivity.this, loginActivity.class);
        startActivity(intent);
        finish();  // Optionally finish the current activity
    }
}
