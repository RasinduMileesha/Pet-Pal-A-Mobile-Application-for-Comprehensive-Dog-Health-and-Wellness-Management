package com.example.petdoc_app;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class VaccineAdapter extends RecyclerView.Adapter<VaccineAdapter.ViewHolder> {

    private List<VaccineData> vaccineDataList;

    public VaccineAdapter(List<VaccineData> vaccineDataList) {
        this.vaccineDataList = vaccineDataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vaccine_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VaccineData data = vaccineDataList.get(position);
        holder.vaccineName.setText(data.getVaccineName());
        holder.vaccineDate.setText(data.getVaccineDate());
        holder.notes.setText(data.getNotes());
        if (data.getImage() != null) {
            holder.imageView.setImageBitmap(data.getImage());
        }
    }

    @Override
    public int getItemCount() {
        return vaccineDataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView vaccineName, vaccineDate, notes;
        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            vaccineName = itemView.findViewById(R.id.vaccineName);
            vaccineDate = itemView.findViewById(R.id.vaccineDate);
            notes = itemView.findViewById(R.id.notes);
            imageView = itemView.findViewById(R.id.vaccineImage);
        }
    }
}
