package com.example.petdoc_app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class vetcenterActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Spinner spinnerProvince;
    private EditText searchLocation;
    private MapView mapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;

    private static final int LOCATION_REQUEST_CODE = 100;

    private final String[] provinces = {
            "Western", "Central", "Southern", "Eastern", "Northern",
            "North Western", "North Central", "Sabaragamuwa", "Uva"
    };

    private final Map<String, List<LatLng>> vetCenters = new HashMap<String, List<LatLng>>() {{
        put("Western", Arrays.asList(
                new LatLng(6.9271, 79.8612), // Colombo
                new LatLng(6.9147, 79.9738), // Negombo
                new LatLng(6.9335, 79.8438), // Dehiwala
                new LatLng(6.9158, 79.8636), // Kotte
                new LatLng(6.9123, 79.8594)  // Mount Lavinia
        ));
        put("Central", Arrays.asList(
                new LatLng(7.2906, 80.6337), // Kandy
                new LatLng(7.3039, 80.6314), // Peradeniya
                new LatLng(7.3398, 80.5765), // Gampola
                new LatLng(7.2968, 80.6022), // Katugastota
                new LatLng(7.2844, 80.6105)  // Wattegama
        ));
        put("Pet Smart", Arrays.asList(
                new LatLng(7.28974359011357, 80.65562073995898)

        ));
        put("Animal Hospital", Arrays.asList(
                new LatLng(7.218245136108902, 79.85059910743892)

        ));
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vetcenter);

        initializeUI();
        initializeLocationClient();
        setupProvinceSpinner();
        initializeMap(savedInstanceState);
        requestLocationPermission();
    }

    private void initializeUI() {
        spinnerProvince = findViewById(R.id.spinnerProvince);
        searchLocation = findViewById(R.id.searchLocation);
        mapView = findViewById(R.id.mapView);

        Button btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(v -> searchVetCenters(searchLocation.getText().toString()));
    }

    private void initializeLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void setupProvinceSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, provinces);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvince.setAdapter(adapter);

        spinnerProvince.setOnItemSelectedListener(new ProvinceSelectionListener());
    }

    private void initializeMap(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            getUserLocation();
        }
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this::loadNearbyVetCenters)
                    .addOnFailureListener(e -> Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show());
        }
    }

    private void loadNearbyVetCenters(Location location) {
        if (location == null) {
            Toast.makeText(this, "Location unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        setMarker(userLatLng, "You are here");

        for (Map.Entry<String, List<LatLng>> entry : vetCenters.entrySet()) {
            for (LatLng vetCenterLatLng : entry.getValue()) {
                double distance = calculateDistance(userLatLng, vetCenterLatLng);
                if (distance <= 10.0) { // Show vet centers within 10 km
                    setMarker(vetCenterLatLng, entry.getKey());
                }
            }
        }

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12));
    }

    private void searchVetCenters(String query) {
        googleMap.clear(); // Clear previous markers
        boolean found = false;

        for (Map.Entry<String, List<LatLng>> entry : vetCenters.entrySet()) {
            if (entry.getKey().toLowerCase().contains(query.toLowerCase())) {
                for (LatLng location : entry.getValue()) {
                    setMarker(location, entry.getKey());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10));
                    found = true;
                }
            }
        }

        if (!found) {
            Toast.makeText(this, "No vet centers found with the specified keyword", Toast.LENGTH_SHORT).show();
        }
    }

    private void setMarker(LatLng position, String title) {
        if (googleMap != null) {
            googleMap.addMarker(new MarkerOptions().position(position).title(title));
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        // Set initial view over Sri Lanka
        LatLng sriLanka = new LatLng(7.8731, 80.7718);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sriLanka, 7));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getUserLocation();
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private double calculateDistance(LatLng latLng1, LatLng latLng2) {
        float[] results = new float[1];
        Location.distanceBetween(latLng1.latitude, latLng1.longitude,
                latLng2.latitude, latLng2.longitude, results);
        return results[0] / 1000; // Convert meters to kilometers
    }

    private class ProvinceSelectionListener implements android.widget.AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
            String selectedProvince = provinces[position];
            googleMap.clear(); // Clear previous markers

            // Show markers for all locations in the selected province
            if (vetCenters.containsKey(selectedProvince)) {
                for (LatLng location : vetCenters.get(selectedProvince)) {
                    setMarker(location, selectedProvince);
                }
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(vetCenters.get(selectedProvince).get(0), 10));
            }
        }

        @Override
        public void onNothingSelected(android.widget.AdapterView<?> parent) {
            // Do nothing
        }
    }
}
