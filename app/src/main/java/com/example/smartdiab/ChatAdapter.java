package com.example.smartdiab;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Message> messages;

    public ChatAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Message message = messages.get(position);
        
        // Handle Text
        if (message.getText() != null) {
            holder.messageText.setVisibility(View.VISIBLE);
            holder.messageText.setText(message.getText());
        } else {
            holder.messageText.setVisibility(View.GONE);
        }

        // Handle Image
        if (message.isImage()) {
            holder.messageImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                 .load(message.getImageUri())
                 .into(holder.messageImage);
        } else {
            holder.messageImage.setVisibility(View.GONE);
        }

        // Layout Alignment (User vs AI)
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.cardView.getLayoutParams();
        if (message.isUser()) {
            params.gravity = Gravity.END;
            holder.cardView.setCardBackgroundColor(0xFF007AFF); // Blue
            holder.messageText.setTextColor(0xFFFFFFFF);
        } else {
            params.gravity = Gravity.START;
            holder.cardView.setCardBackgroundColor(0xFFE9E9EB); // Grey
            holder.messageText.setTextColor(0xFF000000);
        }
        holder.cardView.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        ImageView messageImage;
        MaterialCardView cardView;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            messageImage = itemView.findViewById(R.id.messageImage);
            cardView = itemView.findViewById(R.id.messageCard);
        }
    }
}