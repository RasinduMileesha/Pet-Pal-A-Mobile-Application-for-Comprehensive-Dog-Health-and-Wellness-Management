package com.example.petdoc_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class DiseaseAdapter extends RecyclerView.Adapter<DiseaseAdapter.DiseaseViewHolder> {

    private ArrayList<Disease> diseaseList;

    public DiseaseAdapter(ArrayList<Disease> diseaseList) {
        this.diseaseList = diseaseList;
    }

    @NonNull
    @Override
    public DiseaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.disease_item, parent, false);
        return new DiseaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiseaseViewHolder holder, int position) {
        Disease disease = diseaseList.get(position);
        holder.nameTextView.setText(disease.getName());
        holder.resultTextView.setText(disease.getPredictionResult());
        holder.timestampTextView.setText(disease.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return diseaseList.size();
    }

    public static class DiseaseViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, resultTextView, timestampTextView;

        public DiseaseViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            resultTextView = itemView.findViewById(R.id.resultTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }
    }
}
