package com.example.petdoc_app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class DiseaseReportActivity extends AppCompatActivity {

    private EditText etPetOwnerName;
    private Button filterButton;
    private RecyclerView recyclerView;
    private DiseaseAdapter diseaseAdapter;
    private ArrayList<Disease> diseaseList;
    private ConnectionHelper connectionHelper;
    private TextView tvVetName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diseasereport);

        // Initialize views
        etPetOwnerName = findViewById(R.id.etName); // Update the ID if this is not the correct field
        filterButton = findViewById(R.id.button);
        recyclerView = findViewById(R.id.recyclerView2);

        connectionHelper = new ConnectionHelper();

        // Initialize disease list and set up RecyclerView
        diseaseList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        diseaseAdapter = new DiseaseAdapter(diseaseList);
        recyclerView.setAdapter(diseaseAdapter);
        tvVetName = findViewById(R.id.userName);

        // Retrieve userId, userName, and loginTime consistently
        int userId = getIntent().getIntExtra("userId", -1);
        String name = getIntent().getStringExtra("userName");
        String loginTime = getIntent().getStringExtra("loginTime");

        tvVetName.setText("Welcome Dr. " + name + "\nLogged in at: " + loginTime);


        // Set filter button click event
        filterButton.setOnClickListener(v -> filterByPetOwner(etPetOwnerName.getText().toString()));
    }

    private void loadDiseaseData(String diseaseName) {
        diseaseList.clear();
        try (Connection con = connectionHelper.conclass()) {
            if (con != null) {
                String query = "SELECT Id, Name, PredictionResult, Timestamp FROM Disease WHERE Name = ?";
                PreparedStatement statement = con.prepareStatement(query);
                statement.setString(1, diseaseName);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    int id = rs.getInt("Id");
                    String name = rs.getString("Name");
                    String predictionResult = rs.getString("PredictionResult");
                    String timestamp = rs.getString("Timestamp");
                    diseaseList.add(new Disease(id, name, predictionResult, timestamp));
                }
                diseaseAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterByPetOwner(String petOwnerName) {
        diseaseList.clear();
        try (Connection con = connectionHelper.conclass()) {
            if (con != null) {
                // Get PetOwnerId by pet owner name
                String petOwnerQuery = "SELECT Id FROM PetOwner WHERE Name = ?";
                PreparedStatement petOwnerStmt = con.prepareStatement(petOwnerQuery);
                petOwnerStmt.setString(1, petOwnerName);
                ResultSet petOwnerRs = petOwnerStmt.executeQuery();

                if (petOwnerRs.next()) {
                    int petOwnerId = petOwnerRs.getInt("Id");

                    // Filter diseases by PetOwnerId
                    String diseaseQuery = "SELECT Id, Name, PredictionResult, Timestamp FROM Disease WHERE PetOwnerId = ?";
                    PreparedStatement diseaseStmt = con.prepareStatement(diseaseQuery);
                    diseaseStmt.setInt(1, petOwnerId);
                    ResultSet diseaseRs = diseaseStmt.executeQuery();

                    while (diseaseRs.next()) {
                        int id = diseaseRs.getInt("Id");
                        String name = diseaseRs.getString("Name");
                        String predictionResult = diseaseRs.getString("PredictionResult");
                        String timestamp = diseaseRs.getString("Timestamp");
                        diseaseList.add(new Disease(id, name, predictionResult, timestamp));
                    }
                    diseaseAdapter.notifyDataSetChanged();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
