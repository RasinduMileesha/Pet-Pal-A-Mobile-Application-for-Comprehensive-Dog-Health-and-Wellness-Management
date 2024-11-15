package com.example.petdoc_app;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class VaccManagerActivity extends AppCompatActivity {

    ConnectionHelper connectionHelper;
    EditText vaccineNameInput, vaccineDateInput, vaccineNotesInput;
    ImageView vaccineImageView, logo;
    Button uploadImageButton, saveButton, deleteButton, updateButton;
    Spinner spinner2;
    Bitmap selectedImage;
    Calendar calendar;
    int year, month, day;
    private TextView tvUserName;
    private int userId;
    private String userName;
    private List<Pet> petList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crudvacc);

        // Initialize UI elements
        vaccineNameInput = findViewById(R.id.vaccineNameInput);
        vaccineDateInput = findViewById(R.id.vaccineDateInput);
        vaccineNotesInput = findViewById(R.id.vaccineNotesInput);
        vaccineImageView = findViewById(R.id.vaccineImageView);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);
        updateButton = findViewById(R.id.updateButton);
        spinner2 = findViewById(R.id.spinner2);
        logo = findViewById(R.id.logo);
        tvUserName = findViewById(R.id.userName);

        connectionHelper = new ConnectionHelper();

        // Retrieve userId and userName from Intent
        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);
        userName = intent.getStringExtra("userName");  // Set class-level userName here
        String loginTime = intent.getStringExtra("loginTime");


        if (userId == -1) {
            Toast.makeText(this, "User ID is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Display the user name and login time
        tvUserName.setText("Welcome, " + userName + "\nLogged in at: " + loginTime);

        // Set up DatePicker for vaccineDateInput
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        vaccineDateInput.setOnClickListener(view -> {
            DatePickerDialog datePicker = new DatePickerDialog(VaccManagerActivity.this,
                    (datePicker1, year, month, day) -> {
                        vaccineDateInput.setText((month + 1) + "/" + day + "/" + year);
                    }, year, month, day);
            datePicker.show();
        });

        // Load pet data
        loadPetData();

        // Upload image button click event
        uploadImageButton.setOnClickListener(v -> {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto, 1);
        });

        // Save button click event
        saveButton.setOnClickListener(v -> saveVaccineData());

        // Update and Delete operations
        updateButton.setOnClickListener(v -> updateVaccineData());
        deleteButton.setOnClickListener(v -> confirmDeletion());
    }
    private void clearFields() {
        vaccineNameInput.setText("");
        vaccineDateInput.setText("");
        vaccineNotesInput.setText("");
        logo.setImageResource(R.drawable.question);
        spinner2.setSelection(0);
        vaccineImageView.setImageResource(R.drawable.question);
    }

    // Load pet names and images for PetOwnerId
    private void loadPetData() {
        Connection connection = connectionHelper.conclass();
        if (connection != null) {
            try {
                String query = "SELECT Name, ImageUri FROM Pet WHERE PetOwnerId = ?";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String petName = rs.getString("Name");
                    byte[] imageBytes = rs.getBytes("ImageUri");

                    // Add pet to the pet list for spinner
                    petList.add(new Pet(petName, imageBytes));
                }

                // Set spinner adapter
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getPetNames());
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner2.setAdapter(adapter);

                // Set image for the first pet (optional)
                if (!petList.isEmpty()) {
                    logo.setImageBitmap(BitmapFactory.decodeByteArray(petList.get(0).getImageUri(), 0, petList.get(0).getImageUri().length));
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private List<String> getPetNames() {
        List<String> petNames = new ArrayList<>();
        for (Pet pet : petList) {
            petNames.add(pet.getName());
        }
        return petNames;
    }

    // Handle image selection from gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1 && data != null) {
            try {
                selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                vaccineImageView.setImageBitmap(selectedImage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Save data to SQL Server
    private void saveVaccineData() {
        Connection connection = connectionHelper.conclass();
        if (connection != null) {
            try {
                // Get selected pet name
                String petName = (String) spinner2.getSelectedItem();
                byte[] petImage = null;

                for (Pet pet : petList) {
                    if (pet.getName().equals(petName)) {
                        petImage = pet.getImageUri();
                        break;
                    }
                }

                String query = "INSERT INTO Vaccinations (vaccineName, vaccineDate, notes, image, PetOwnerId, PetName, ImageUri, userName) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, vaccineNameInput.getText().toString());
                preparedStatement.setString(2, vaccineDateInput.getText().toString());
                preparedStatement.setString(3, vaccineNotesInput.getText().toString());

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                if (selectedImage != null) {
                    selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    preparedStatement.setBytes(4, byteArrayOutputStream.toByteArray());
                } else {
                    preparedStatement.setBytes(4, null);
                }

                preparedStatement.setInt(5, userId); // PetOwnerId
                preparedStatement.setString(6, petName); // PetName
                preparedStatement.setBytes(7, petImage); // ImageUri
                preparedStatement.setString(8, userName); // userName added here

                preparedStatement.executeUpdate();
                Toast.makeText(this, "Data Saved Successfully", Toast.LENGTH_SHORT).show();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    // Update data in SQL Server
    private void updateVaccineData() {
        Connection connection = connectionHelper.conclass();
        if (connection != null) {
            try {
                String query = "UPDATE Vaccinations SET vaccineDate = ?, notes = ?, image = ? WHERE vaccineName = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, vaccineDateInput.getText().toString());
                preparedStatement.setString(2, vaccineNotesInput.getText().toString());

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                if (selectedImage != null) {
                    selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    preparedStatement.setBytes(3, byteArrayOutputStream.toByteArray());
                } else {
                    preparedStatement.setBytes(3, null);
                }

                preparedStatement.setString(4, vaccineNameInput.getText().toString());
                preparedStatement.executeUpdate();
                Toast.makeText(this, "Data Updated Successfully", Toast.LENGTH_SHORT).show();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void confirmDeletion() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this pet?")
                .setPositiveButton("Yes", (dialog, which) -> deleteVaccineData())
                .setNegativeButton("No", null)
                .show();
    }

    // Delete data from SQL Server
    private void deleteVaccineData() {
        Connection connection = connectionHelper.conclass();
        if (connection != null) {
            try {
                String query = "DELETE FROM Vaccinations WHERE vaccineName = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, vaccineNameInput.getText().toString());
                preparedStatement.executeUpdate();
                Toast.makeText(this, "Data Deleted Successfully", Toast.LENGTH_SHORT).show();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Pet class to manage pet name and image
    private static class Pet {
        private final String name;
        private final byte[] imageUri;

        public Pet(String name, byte[] imageUri) {
            this.name = name;
            this.imageUri = imageUri;
        }

        public String getName() {
            return name;
        }

        public byte[] getImageUri() {
            return imageUri;
        }
    }
}
