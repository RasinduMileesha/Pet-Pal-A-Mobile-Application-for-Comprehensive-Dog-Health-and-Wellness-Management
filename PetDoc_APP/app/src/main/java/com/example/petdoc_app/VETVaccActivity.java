package com.example.petdoc_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VETVaccActivity extends AppCompatActivity {

    private ConnectionHelper connectionHelper;
    private ImageView logoImageView;
    private Spinner spinner4;
    private RecyclerView recyclerView3;
    private VaccineAdapter vaccineAdapter;
    private List<VaccineData> vaccineDataList;
    private EditText etName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vetvacview);

        // Initialize views
        logoImageView = findViewById(R.id.logo);
        spinner4 = findViewById(R.id.spinner4);
        recyclerView3 = findViewById(R.id.recyclerView3);
        etName = findViewById(R.id.etName);
        Button btnFind = findViewById(R.id.btnFind);

        // Initialize RecyclerView and adapter
        recyclerView3.setLayoutManager(new LinearLayoutManager(this));
        vaccineDataList = new ArrayList<>();
        vaccineAdapter = new VaccineAdapter(vaccineDataList);
        recyclerView3.setAdapter(vaccineAdapter);

        // Initialize ConnectionHelper
        connectionHelper = new ConnectionHelper();

        // Set a click listener on the "Find" button
        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = etName.getText().toString().trim();
                if (userName.isEmpty()) {
                    Toast.makeText(VETVaccActivity.this, "Please enter a name", Toast.LENGTH_SHORT).show();
                } else {
                    // Load data using the input from etName
                    new LoadDataTask(userName).execute();
                }
            }
        });
    }

    // Combined AsyncTask to load PetData and VaccineData
    @SuppressLint("StaticFieldLeak")
    private class LoadDataTask extends AsyncTask<Void, Void, Boolean> {

        private final String userName;
        private List<String> petNames = new ArrayList<>();
        private Bitmap logoImage = null;
        private List<VaccineData> vaccineData = new ArrayList<>();

        // Constructor to initialize with the provided userName
        LoadDataTask(String userName) {
            this.userName = userName;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try (Connection connection = connectionHelper.conclass()) {
                if (connection != null) {
                    loadPetData(connection);
                    loadVaccineData(connection);
                    return true;
                } else {
                    Log.e("VETVaccActivity", "Database connection failed");
                }
            } catch (SQLException e) {
                Log.e("VETVaccActivity", "Error loading data: ", e);
            }
            return false;
        }

        private void loadPetData(Connection connection) throws SQLException {
            String petQuery = "SELECT PetName, ImageUri FROM Vaccinations WHERE userName = ?";
            try (PreparedStatement petStatement = connection.prepareStatement(petQuery)) {
                petStatement.setString(1, userName);
                try (ResultSet petResultSet = petStatement.executeQuery()) {
                    while (petResultSet.next()) {
                        petNames.add(petResultSet.getString("PetName"));
                        if (logoImage == null) {
                            byte[] imageBytes = petResultSet.getBytes("ImageUri");
                            logoImage = decodeImage(imageBytes);
                        }
                    }
                }
            }
        }

        private void loadVaccineData(Connection connection) throws SQLException {
            String vaccineQuery = "SELECT vaccineName, vaccineDate, notes, image FROM Vaccinations WHERE userName = ?";
            try (PreparedStatement vaccineStatement = connection.prepareStatement(vaccineQuery)) {
                vaccineStatement.setString(1, userName);
                try (ResultSet vaccineResultSet = vaccineStatement.executeQuery()) {
                    while (vaccineResultSet.next()) {
                        String vaccineName = vaccineResultSet.getString("vaccineName");
                        String vaccineDate = vaccineResultSet.getString("vaccineDate");
                        String notes = vaccineResultSet.getString("notes");

                        byte[] imageBytes = vaccineResultSet.getBytes("image");
                        Bitmap vaccineImage = decodeImage(imageBytes);

                        vaccineData.add(new VaccineData(vaccineName, vaccineDate, notes, vaccineImage));
                    }
                }
            }
        }

        private Bitmap decodeImage(byte[] imageBytes) {
            if (imageBytes != null) {
                return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) {
                Toast.makeText(VETVaccActivity.this, "Failed to load data from database", Toast.LENGTH_SHORT).show();
                return;
            }

            if (logoImage != null) {
                logoImageView.setImageBitmap(logoImage);
            } else {
                logoImageView.setImageResource(R.drawable.question); // Default image
            }

            // Populate spinner with pet names
            ArrayAdapter<String> adapter = new ArrayAdapter<>(VETVaccActivity.this, android.R.layout.simple_spinner_item, petNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner4.setAdapter(adapter);

            // Update RecyclerView with vaccination data
            vaccineDataList.clear();
            vaccineDataList.addAll(vaccineData);
            vaccineAdapter.notifyDataSetChanged();
        }
    }
}
