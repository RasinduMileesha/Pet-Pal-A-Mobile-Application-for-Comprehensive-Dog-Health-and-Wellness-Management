package com.example.petdoc_app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class crudPetActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_GALLERY = 1;
    private static final int REQUEST_IMAGE_CAMERA = 2;
    private static final int REQUEST_PERMISSIONS = 100;

    private EditText etName, etColor, etDate;
    private Spinner spinnerBreed, selectPetSpinner;
    private RadioGroup genderGroup;
    private ImageView petPreview;
    private Button btnUpdate, btnDelete, btnUploadImage, btnCamera;
    private int selectedPetId;
    private ConnectionHelper connectionHelper;
    private HashMap<String, Integer> petMap;
    private int userId;
    private Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crudpet);

        initializeUIElements();
        connectionHelper = new ConnectionHelper();
        petMap = new HashMap<>();

        userId = getIntent().getIntExtra("userId", -1);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions();
        }

        loadPetNames();
        setupListeners();
    }

    private void initializeUIElements() {
        etName = findViewById(R.id.etName);
        etColor = findViewById(R.id.etColor);
        etDate = findViewById(R.id.etDate);
        spinnerBreed = findViewById(R.id.spinner);
        selectPetSpinner = findViewById(R.id.selectPet);
        genderGroup = findViewById(R.id.genderGroup);
        petPreview = findViewById(R.id.PetPreview);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        btnCamera = findViewById(R.id.btnCamera);

        ArrayAdapter<String> breedAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Select Breed", "Labrador", "Poodle", "German Shepherd", "Bulldog"});
        breedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBreed.setAdapter(breedAdapter);
    }

    private void setupListeners() {
        etDate.setOnClickListener(view -> showDatePicker());

        selectPetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedPetName = parent.getItemAtPosition(position).toString();
                selectedPetId = petMap.getOrDefault(selectedPetName, -1);
                retrievePetData(selectedPetId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnUpdate.setOnClickListener(view -> updatePet());
        btnDelete.setOnClickListener(view -> confirmDeletion());
        btnUploadImage.setOnClickListener(view -> selectImageFromGallery());
        btnCamera.setOnClickListener(view -> captureImageFromCamera());
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            etDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void clearFields() {
        etName.setText("");
        etColor.setText("");
        etDate.setText("");
        spinnerBreed.setSelection(0);
        genderGroup.clearCheck();
        petPreview.setImageResource(R.drawable.question);
    }

    private void loadPetNames() {
        try (Connection connection = connectionHelper.conclass()) {
            if (connection == null) {
                showToast("Unable to connect to the database.");
                return;
            }

            petMap.clear();
            String query = "SELECT PetId, Name FROM Pet WHERE PetOwnerId = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, userId);
                ResultSet resultSet = statement.executeQuery();

                List<String> petNames = new ArrayList<>();
                while (resultSet.next()) {
                    int petId = resultSet.getInt("PetId");
                    String petName = resultSet.getString("Name");
                    petNames.add(petName);
                    petMap.put(petName, petId);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, petNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                selectPetSpinner.setAdapter(adapter);
            }
        } catch (SQLException e) {
            Log.e("SQL Error", e.getMessage());
            showToast("Error loading pet names.");
        }
    }

    private void retrievePetData(int petId) {
        if (petId == -1) return;

        try (Connection connection = connectionHelper.conclass()) {
            if (connection == null) {
                showToast("Unable to connect to the database.");
                return;
            }

            String query = "SELECT * FROM Pet WHERE PetId = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, petId);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    populatePetData(resultSet);
                }
            }
        } catch (SQLException e) {
            Log.e("RetrievePetData", "Error retrieving pet data", e);
            showToast("Error retrieving pet data.");
        }
    }

    private void populatePetData(ResultSet resultSet) throws SQLException {
        etName.setText(resultSet.getString("Name"));
        etColor.setText(resultSet.getString("Color"));
        etDate.setText(resultSet.getString("DateOfBirth"));

        String breed = resultSet.getString("Breed");
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerBreed.getAdapter();
        spinnerBreed.setSelection(adapter.getPosition(breed));

        String gender = resultSet.getString("Gender");
        if ("Male".equals(gender)) ((RadioButton) findViewById(R.id.male)).setChecked(true);
        else ((RadioButton) findViewById(R.id.female)).setChecked(true);

        byte[] imageBytes = resultSet.getBytes("ImageUri");
        if (imageBytes != null && imageBytes.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(imageBytes));
            petPreview.setImageBitmap(bitmap);
        } else {
            petPreview.setImageResource(R.drawable.question);
        }
    }

    private void updatePet() {
        String name = etName.getText().toString();
        String color = etColor.getText().toString();
        String dateOfBirth = etDate.getText().toString();
        String breed = spinnerBreed.getSelectedItem().toString();
        String gender = ((RadioButton) findViewById(genderGroup.getCheckedRadioButtonId())).getText().toString();

        petPreview.setDrawingCacheEnabled(true);
        petPreview.buildDrawingCache();
        Bitmap bitmap = petPreview.getDrawingCache();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] imageBytes = stream.toByteArray();
        petPreview.setDrawingCacheEnabled(false);

        try (Connection connection = connectionHelper.conclass()) {
            if (connection == null) {
                showToast("Unable to connect to the database.");
                return;
            }

            String query = "UPDATE Pet SET Name = ?, Color = ?, Gender = ?, DateOfBirth = ?, Breed = ?, ImageUri = ? WHERE PetId = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, name);
                statement.setString(2, color);
                statement.setString(3, gender);
                statement.setString(4, dateOfBirth);
                statement.setString(5, breed);
                statement.setBytes(6, imageBytes);
                statement.setInt(7, selectedPetId);

                int rowsUpdated = statement.executeUpdate();
                if (rowsUpdated > 0) {
                    showToast("Pet updated successfully");
                    loadPetNames();
                } else {
                    showToast("Update failed.");
                }
            }
        } catch (SQLException e) {
            Log.e("Update Error", e.getMessage());
            showToast("Database error occurred.");
        }
    }

    private void confirmDeletion() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this pet?")
                .setPositiveButton("Yes", (dialog, which) -> deletePet())
                .setNegativeButton("No", null)
                .show();
    }

    private void deletePet() {
        try (Connection connection = connectionHelper.conclass()) {
            if (connection == null) {
                showToast("Unable to connect to the database.");
                return;
            }

            String query = "DELETE FROM Pet WHERE PetId = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, selectedPetId);

                int rowsDeleted = statement.executeUpdate();
                if (rowsDeleted > 0) {
                    showToast("Pet deleted successfully");
                    loadPetNames();
                    clearFields();
                } else {
                    showToast("Deletion failed.");
                }
            }
        } catch (SQLException e) {
            Log.e("Delete Error", e.getMessage());
            showToast("Database error occurred.");
        }
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE
            }, REQUEST_PERMISSIONS);
        }
    }

    private void selectImageFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_IMAGE_GALLERY);
    }

    private void captureImageFromCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAMERA);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_GALLERY && data != null) {
                Uri selectedImageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    petPreview.setImageBitmap(bitmap);
                } catch (IOException e) {
                    Log.e("Image Selection Error", e.getMessage());
                }
            } else if (requestCode == REQUEST_IMAGE_CAMERA && data != null) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                petPreview.setImageBitmap(bitmap);
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                showToast("Permission is required for accessing camera and gallery.");
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
