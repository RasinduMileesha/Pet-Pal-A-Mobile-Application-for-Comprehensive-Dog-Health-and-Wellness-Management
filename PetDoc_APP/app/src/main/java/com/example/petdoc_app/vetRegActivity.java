package com.example.petdoc_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class vetRegActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ConnectionHelper connectionHelper;
    private EditText vetIdInput, nameInput, passwordInput, mobileInput, cityInput, gmailInput, etRegistrationDate;
    private byte[] imageBytes = null;
    private TextView loginText;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vetregister);

        connectionHelper = new ConnectionHelper();

        // Initialize EditText fields
        vetIdInput = findViewById(R.id.vetID);
        nameInput = findViewById(R.id.etName);
        mobileInput = findViewById(R.id.etMobile);
        gmailInput = findViewById(R.id.etGmail);
        cityInput = findViewById(R.id.etCityProvince);
        passwordInput = findViewById(R.id.password);
        loginText = findViewById(R.id.loginText);
        etRegistrationDate = findViewById(R.id.etRegistrationDate);

        // Set current date for the registration date field
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        etRegistrationDate.setText(currentDate);
        etRegistrationDate.setEnabled(false);
        etRegistrationDate.setTextColor(getResources().getColor(android.R.color.black));
        etRegistrationDate.setTextSize(18);
        etRegistrationDate.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
        etRegistrationDate.setGravity(android.view.Gravity.CENTER);

        // Set up the "Upload Image" button
        findViewById(R.id.btnUploadImage).setOnClickListener(v -> openImageChooser());

        // Set up the "Register" button
        Button registerButton = findViewById(R.id.btnRegister);
        registerButton.setOnClickListener(v -> registerVet());

        // Set click listener for the loginText to redirect to LoginActivity
        loginText.setOnClickListener(view -> {
            loginText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            startActivity(new Intent(vetRegActivity.this, loginActivity.class));
        });
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageBytes = getBytesFromBitmap(bitmap);
                Toast.makeText(this, "Image selected successfully!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e("Image Error", e.getMessage());
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    private void registerVet() {
        String vetId = vetIdInput.getText().toString();
        String name = nameInput.getText().toString();
        String password = passwordInput.getText().toString();
        String mobile = mobileInput.getText().toString();
        String city = cityInput.getText().toString();
        String gmail = gmailInput.getText().toString();
        String registrationDate = etRegistrationDate.getText().toString();

        // Validation checks
        if (vetId.isEmpty() || name.isEmpty() || password.isEmpty() || mobile.isEmpty() || city.isEmpty() || gmail.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(gmail).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mobile.length() < 10) {
            Toast.makeText(this, "Please enter a valid mobile number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageBytes == null) {
            Toast.makeText(this, "Please upload an image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save vet data in the database
        saveVetToDatabase(vetId, name, password, mobile, city, gmail, imageBytes, registrationDate);
    }

    private void saveVetToDatabase(String vetId, String name, String password, String mobile, String city, String gmail, byte[] image, String registrationDate) {
        Connection connection = connectionHelper.conclass();

        if (connection != null) {
            try {
                String query = "INSERT INTO vets (VetId, Name, Password, Mobile, CityProvince, Gmail, Image, RegistrationDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = connection.prepareStatement(query);

                pstmt.setString(1, vetId);
                pstmt.setString(2, name);
                pstmt.setString(3, password);
                pstmt.setString(4, mobile);
                pstmt.setString(5, city);
                pstmt.setString(6, gmail);
                pstmt.setBytes(7, image);
                pstmt.setString(8, registrationDate);

                int result = pstmt.executeUpdate();
                if (result > 0) {
                    Toast.makeText(this, "Vet registered successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(vetRegActivity.this, loginActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Failed to register vet", Toast.LENGTH_SHORT).show();
                }

                pstmt.close();
                connection.close();
            } catch (Exception e) {
                Log.e("SQL Error", e.getMessage());
                Toast.makeText(this, "An error occurred. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Failed to connect to the database.", Toast.LENGTH_SHORT).show();
        }
    }
}
