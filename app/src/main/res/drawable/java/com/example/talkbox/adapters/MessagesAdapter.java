package com.example.talkbox.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.talkbox.R;
import com.example.talkbox.models.Message;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<Message> messages;
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    public MessagesAdapter(Context context, ArrayList<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getSenderId().equals(FirebaseAuth.getInstance().getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.msg_sender_item, parent, false);
            return new RightViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.msg_receiver_item, parent, false);
            return new LeftViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String time = sdf.format(new Date(message.getTimestamp()));

        if (holder.getClass() == RightViewHolder.class) {
            ((RightViewHolder) holder).showMessage.setText(message.getMessage());
            ((RightViewHolder) holder).showTimestamp.setText(time);
        } else {
            ((LeftViewHolder) holder).showMessage.setText(message.getMessage());
            ((LeftViewHolder) holder).showTimestamp.setText(time);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class RightViewHolder extends RecyclerView.ViewHolder {
        TextView showMessage;
        TextView showTimestamp;

        public RightViewHolder(@NonNull View itemView) {
            super(itemView);
            showMessage = itemView.findViewById(R.id.message);
            showTimestamp = itemView.findViewById(R.id.timestamp);
        }
    }

    public static class LeftViewHolder extends RecyclerView.ViewHolder {
        TextView showMessage;
        TextView showTimestamp;

        public LeftViewHolder(@NonNull View itemView) {
            super(itemView);
            showMessage = itemView.findViewById(R.id.message);
            showTimestamp = itemView.findViewById(R.id.timestamp);
        }
    }
}
