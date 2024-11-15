package com.example.petdoc_app;

import android.graphics.Bitmap;

public class VaccineData {
    private String vaccineName;
    private String vaccineDate;
    private String notes;
    private Bitmap image;

    public VaccineData(String vaccineName, String vaccineDate, String notes, Bitmap image) {
        this.vaccineName = vaccineName;
        this.vaccineDate = vaccineDate;
        this.notes = notes;
        this.image = image;
    }

    public String getVaccineName() { return vaccineName; }
    public String getVaccineDate() { return vaccineDate; }
    public String getNotes() { return notes; }
    public Bitmap getImage() { return image; }
}
