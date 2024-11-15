package com.example.petdoc_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petdoc_app.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

public class DiseaseDetectActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final float CONFIDENCE_THRESHOLD = 0.7f;

    private ImageView imgView;
    private TextView predictionResult, remedies, welcomeText;
    private Button captureBtn, uploadBtn, vetCenterBtn;
    private Bitmap imageBitmap;
    private Spinner spinner5;
    private int userId;

    private final String[] diseaseNames = {
            "Alabama Rot Disease",
            "Skin Rashes",
            "Alopecia Disease",
            "Tick-Borne Disease",
            "Ear Infection Disease",
            "Skin Cancer"
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diseaseml);

        imgView = findViewById(R.id.imgView);
        predictionResult = findViewById(R.id.predictionResult);
        remedies = findViewById(R.id.remedies);
        welcomeText = findViewById(R.id.welcomeText);
        captureBtn = findViewById(R.id.captureBtn);
        uploadBtn = findViewById(R.id.uploadBtn);
        vetCenterBtn = findViewById(R.id.vetCenterBtn);
        spinner5 = findViewById(R.id.spinner5);

        captureBtn.setOnClickListener(v -> captureImage());
        uploadBtn.setOnClickListener(v -> uploadImage());
        vetCenterBtn.setOnClickListener(v -> openVetCenter());

        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);
        String userName = intent.getStringExtra("userName");
        String loginTime = intent.getStringExtra("loginTime");

        if (userId == -1) {
            Toast.makeText(this, "User ID is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        welcomeText.setText("Welcome, " + userName + "\nLogged in at: " + loginTime);
        loadPetOwnerName(userId);
    }

    private void captureImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void uploadImage() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data.getExtras() != null) {
                imageBitmap = (Bitmap) data.getExtras().get("data");
                imgView.setImageBitmap(imageBitmap);
                predictDisease();
            } else if (requestCode == REQUEST_IMAGE_PICK && data.getData() != null) {
                try {
                    imageBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(data.getData()));
                    imgView.setImageBitmap(imageBitmap);
                    predictDisease();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadPetOwnerName(int userId) {
        String petName = getPetOwnerNameById(userId);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Collections.singletonList(petName));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner5.setAdapter(adapter);
    }

    private String getPetOwnerNameById(int userId) {
        String petName = "Unknown";
        ConnectionHelper connectionHelper = new ConnectionHelper();
        Connection connection = connectionHelper.conclass();

        if (connection != null) {
            try {
                String query = "SELECT Name FROM Pet WHERE PetOwnerId = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, userId);

                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    petName = resultSet.getString("Name");
                }

                resultSet.close();
                preparedStatement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error retrieving pet name", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
        }

        return petName;
    }

    private void openVetCenter() {
        Toast.makeText(this, "Navigating to Vet Center", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(DiseaseDetectActivity.this, vetcenterActivity.class);
        startActivity(intent);
    }

    private void predictDisease() {
        if (imageBitmap != null) {
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 224, 224, true);
            TensorImage tensorImage = TensorImage.fromBitmap(resizedBitmap);

            try {
                Model model = Model.newInstance(this);
                TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.UINT8);
                inputFeature0.loadBuffer(tensorImage.getBuffer());

                Model.Outputs outputs = model.process(inputFeature0);
                TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                float[] confidences = outputFeature0.getFloatArray();
                int maxIndex = getMaxConfidenceIndex(confidences);
                float maxConfidence = confidences[maxIndex];

                String predictionText;
                if (maxConfidence >= CONFIDENCE_THRESHOLD) {
                    predictionText = diseaseNames[maxIndex];
                    predictionResult.setText(predictionText);

                    // Load treatment description based on predicted disease
                    loadTreatmentDescription(predictionText);
                } else {
                    predictionText = "Unrecognized image. Please provide a relevant image.";
                    predictionResult.setText(predictionText);
                    Toast.makeText(this, "Image not recognized. Please provide an image related to pet diseases.", Toast.LENGTH_LONG).show();
                }

                String petOwnerName = (String) spinner5.getSelectedItem();
                storePredictionInDatabase(userId, petOwnerName, predictionText);

                model.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadTreatmentDescription(String diseaseName) {
        ConnectionHelper connectionHelper = new ConnectionHelper();
        Connection connection = connectionHelper.conclass();

        if (connection != null) {
            try {
                String query = "SELECT Description FROM Treatment WHERE DiseaseName = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, diseaseName);

                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    String description = resultSet.getString("Description");
                    remedies.setText(description);
                } else {
                    remedies.setText("No treatment information available for " + diseaseName);
                }

                resultSet.close();
                preparedStatement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error retrieving treatment description", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void storePredictionInDatabase(int petOwnerId, String name, String predictionResult) {
        ConnectionHelper connectionHelper = new ConnectionHelper();
        Connection connection = connectionHelper.conclass();

        if (connection != null) {
            try {
                String query = "INSERT INTO Disease (PetOwnerId, Name, PredictionResult, Timestamp) VALUES (?, ?, ?, GETDATE())";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, petOwnerId);
                preparedStatement.setString(2, name);
                preparedStatement.setString(3, predictionResult);

                preparedStatement.executeUpdate();
                Toast.makeText(this, "Record saved successfully", Toast.LENGTH_SHORT).show();

                preparedStatement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error saving record", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
        }
    }

    private int getMaxConfidenceIndex(float[] confidenceArray) {
        int maxIndex = 0;
        float maxConfidence = confidenceArray[0];
        for (int i = 1; i < confidenceArray.length; i++) {
            if (confidenceArray[i] > maxConfidence) {
                maxConfidence = confidenceArray[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }
}
