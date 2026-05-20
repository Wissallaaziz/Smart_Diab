package com.example.smartdiab;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<HistoryItem> historyList;

    public HistoryAdapter(List<HistoryItem> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryItem item = historyList.get(position);
        holder.mealName.setText(item.meal_name);
        holder.date.setText(item.timestamp);
        holder.verdict.setText(item.verdict);
        
        // Coloration dynamique du verdict
        if (item.verdict.contains("Autorisé")) {
            holder.verdict.setTextColor(0xFF4CAF50);
        } else if (item.verdict.contains("Modéré")) {
            holder.verdict.setTextColor(0xFFFFC107);
        } else {
            holder.verdict.setTextColor(0xFFF44336);
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mealName, date, verdict;
        public ViewHolder(View itemView) {
            super(itemView);
            mealName = itemView.findViewById(R.id.historyMealName);
            date = itemView.findViewById(R.id.historyDate);
            verdict = itemView.findViewById(R.id.historyVerdict);
        }
    }
}