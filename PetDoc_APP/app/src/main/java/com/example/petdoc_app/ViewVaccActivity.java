package com.example.petdoc_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
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

public class ViewVaccActivity extends AppCompatActivity {

    private int userId;
    private ConnectionHelper connectionHelper;
    private ImageView logoImageView;
    private Spinner spinner3;
    private RecyclerView recyclerView;
    private VaccineAdapter vaccineAdapter;
    private List<VaccineData> vaccineDataList;
    private TextView tvUserName;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewvacc); // Update this with the correct layout name if needed

        // Retrieve userId from Intent
        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);
        String userName = intent.getStringExtra("userName");
        String loginTime = intent.getStringExtra("loginTime");

        // Initialize views
        logoImageView = findViewById(R.id.logo);
        spinner3 = findViewById(R.id.spinner3);
        recyclerView = findViewById(R.id.recyclerView2);
        tvUserName = findViewById(R.id.userName);

        if (userId == -1) {
            Toast.makeText(this, "User ID is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Display the user name and login time
        tvUserName.setText("Welcome, " + userName + "\nLogged in at: " + loginTime);

        // Initialize RecyclerView and adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        vaccineDataList = new ArrayList<>();
        vaccineAdapter = new VaccineAdapter(vaccineDataList);
        recyclerView.setAdapter(vaccineAdapter);

        // Load data
        connectionHelper = new ConnectionHelper();
        new LoadPetDataTask().execute();
        new LoadVaccineDataTask().execute();
    }

    // AsyncTask to load PetName for spinner and ImageUri for logo
    private class LoadPetDataTask extends AsyncTask<Void, Void, Void> {
        private List<String> petNames = new ArrayList<>();
        private Bitmap logoImage = null;

        @Override
        protected Void doInBackground(Void... voids) {
            try (Connection connection = connectionHelper.conclass()) {
                if (connection != null) {
                    String query = "SELECT PetName, ImageUri FROM Vaccinations WHERE PetOwnerId = ?";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setInt(1, userId);
                    ResultSet resultSet = statement.executeQuery();

                    while (resultSet.next()) {
                        // Retrieve pet name and add to list
                        petNames.add(resultSet.getString("PetName"));

                        // Retrieve and decode ImageUri (if not already loaded)
                        if (logoImage == null) {
                            byte[] imageBytes = resultSet.getBytes("ImageUri");
                            if (imageBytes != null) {
                                logoImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                            }
                        }
                    }
                } else {
                    publishProgress(); // Notify on connection failure
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Toast.makeText(ViewVaccActivity.this, "Failed to load data from database", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (logoImage != null) {
                logoImageView.setImageBitmap(logoImage);
            } else {
                logoImageView.setImageResource(R.drawable.question); // Replace with your default logo
            }

            // Set data in spinner
            ArrayAdapter<String> adapter = new ArrayAdapter<>(ViewVaccActivity.this, android.R.layout.simple_spinner_item, petNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner3.setAdapter(adapter);
        }
    }

    // AsyncTask to load Vaccination data for RecyclerView
    private class LoadVaccineDataTask extends AsyncTask<Void, Void, List<VaccineData>> {

        @Override
        protected List<VaccineData> doInBackground(Void... voids) {
            List<VaccineData> data = new ArrayList<>();
            try (Connection connection = connectionHelper.conclass()) {
                if (connection != null) {
                    String query = "SELECT vaccineName, vaccineDate, notes, image FROM Vaccinations WHERE PetOwnerId = ?";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setInt(1, userId);
                    ResultSet resultSet = statement.executeQuery();

                    while (resultSet.next()) {
                        String vaccineName = resultSet.getString("vaccineName");
                        String vaccineDate = resultSet.getString("vaccineDate");
                        String notes = resultSet.getString("notes");

                        byte[] imageBytes = resultSet.getBytes("image");
                        Bitmap vaccineImage = null;
                        if (imageBytes != null) {
                            vaccineImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        }

                        data.add(new VaccineData(vaccineName, vaccineDate, notes, vaccineImage));
                    }
                } else {
                    // Handle connection failure if needed
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(List<VaccineData> data) {
            vaccineDataList.clear();
            vaccineDataList.addAll(data);
            vaccineAdapter.notifyDataSetChanged();
        }
    }
}
