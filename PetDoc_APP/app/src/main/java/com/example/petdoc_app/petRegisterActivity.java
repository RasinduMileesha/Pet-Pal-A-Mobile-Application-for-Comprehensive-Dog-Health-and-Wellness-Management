package com.example.petdoc_app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class petRegisterActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private static final int CAPTURE_IMAGE = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    private EditText etName, etColor, etDate;
    private RadioGroup genderGroup;
    private Spinner spinner;
    private Button btnRegister, btnUploadImage, btnCamera;
    private ImageView petImage;
    private Uri imageUri;
    private String currentPhotoPath;
    private ProgressDialog progressDialog;
    private int userId;

    // Breed list declaration
    private final String[] breedList = new String[]{
            "Select Breed...",
            "Labrador Retriever",
            "Poodle",
            "German Shepherd",
            "Bulldog",
            "Golden Retriever",
            "Beagle",
            "Rottweiler",
            "Yorkshire Terrier",
            "Boxer",
            "Dachshund",
            "Shih Tzu",
            "Siberian Husky",
            "Chihuahua",
            "Pug",
            "Doberman Pinscher",
            "Great Dane",
            "Australian Shepherd",
            "Cocker Spaniel",
            "Maltese",
            "Border Collie",
            "Shiba Inu",
            "Pomeranian",
            "Basset Hound",
            "French Bulldog",
            "Cavalier King Charles Spaniel",
            "Boston Terrier",
            "Shetland Sheepdog",
            "Bernese Mountain Dog",
            "Havanese",
            "English Springer Spaniel",
            "Brittany Spaniel",
            "American Staffordshire Terrier",
            "Bloodhound",
            "Newfoundland",
            "Whippet",
            "Saint Bernard",
            "Akita",
            "Old English Sheepdog",
            "Irish Setter",
            "Papillon",
            "Weimaraner",
            "Samoyed",
            "Greyhound",
            "Pointer",
            "Bullmastiff",
            "Pekingese",
            "Vizsla",
            "Bichon Frise",
            "Collie",
            "Rhodesian Ridgeback",
            "Mastiff"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.petregister);

        userId = getIntent().getIntExtra("userId", -1);
        if (userId == -1) {
            Toast.makeText(this, "User ID is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        petImage = findViewById(R.id.PetPreview);
        etName = findViewById(R.id.etName);
        etColor = findViewById(R.id.etColor);
        etDate = findViewById(R.id.etDate);
        genderGroup = findViewById(R.id.genderGroup);
        spinner = findViewById(R.id.spinner);
        btnRegister = findViewById(R.id.btnRegister);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        btnCamera = findViewById(R.id.btnCamera);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering Pet...");

        // Setup breed spinner with breedList array
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, breedList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        etDate.setOnClickListener(v -> showDatePicker());
        btnUploadImage.setOnClickListener(v -> openImageSelector());
        btnCamera.setOnClickListener(v -> openCamera());
        btnRegister.setOnClickListener(v -> registerPet());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) ->
                etDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year),
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void openImageSelector() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Camera permission is required to take pictures", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                File photoFile = createImageFile();
                imageUri = FileProvider.getUriForFile(this,
                        "com.example.petdoc_app.fileprovider", photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

                Log.d("CameraIntent", "Launching camera with URI: " + imageUri);
                startActivityForResult(takePictureIntent, CAPTURE_IMAGE);
            } catch (IOException ex) {
                Log.e("CameraIntent", "Error creating photo file", ex);
                Toast.makeText(this, "Error creating photo file", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) storageDir.mkdirs();
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        Log.d("CameraIntent", "Created image file path: " + currentPhotoPath);
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE && data != null && data.getData() != null) {
                imageUri = data.getData();
                petImage.setImageURI(imageUri);
            } else if (requestCode == CAPTURE_IMAGE && imageUri != null) {
                petImage.setImageURI(imageUri);
                Log.d("CameraIntent", "Image captured and displayed in ImageView");
            }
        } else {
            Toast.makeText(this, "Image selection cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] getImageBytes(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream()) {
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            return byteBuffer.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void registerPet() {
        String petName = etName.getText().toString().trim();
        String color = etColor.getText().toString().trim();
        String dateOfBirth = etDate.getText().toString().trim();
        String breed = spinner.getSelectedItem().toString();
        int selectedGenderId = genderGroup.getCheckedRadioButtonId();

        if (petName.isEmpty() || color.isEmpty() || dateOfBirth.isEmpty() ||
                imageUri == null || breed.equals("Select Breed") || selectedGenderId == -1) {
            Toast.makeText(this, "Please fill all fields and select a valid breed", Toast.LENGTH_SHORT).show();
            return;
        }

        String gender = ((RadioButton) findViewById(selectedGenderId)).getText().toString();

        progressDialog.show();

        new Thread(() -> {
            Connection connection = null;
            PreparedStatement statement = null;
            try {
                connection = new ConnectionHelper().conclass();
                if (connection != null) {
                    byte[] imageBytes = getImageBytes(imageUri);

                    String query = "INSERT INTO Pet (PetOwnerId, Name, Color, Gender, DateOfBirth, Breed, ImageUri) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    statement = connection.prepareStatement(query);
                    statement.setInt(1, userId);
                    statement.setString(2, petName);
                    statement.setString(3, color);
                    statement.setString(4, gender);
                    statement.setString(5, dateOfBirth);
                    statement.setString(6, breed);
                    statement.setBytes(7, imageBytes);

                    int rowsInserted = statement.executeUpdate();
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        if (rowsInserted > 0) {
                            Toast.makeText(this, "Pet registered successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to register pet", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (SQLException e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } finally {
                try {
                    if (statement != null) statement.close();
                    if (connection != null) connection.close();
                } catch (SQLException ignored) {
                }
            }
        }).start();
    }
}
