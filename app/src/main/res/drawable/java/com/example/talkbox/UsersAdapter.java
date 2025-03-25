package com.example.talkbox;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.talkbox.R;
import com.example.talkbox.User;
import com.example.talkbox.User;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {

    private Context context;
    private ArrayList<User> users;

    public UsersAdapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_item, parent, false);
        return new UsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
        User user = users.get(position);
        holder.name.setText(user.getName());
        holder.about.setText(user.getPhoneNumber());
        Glide.with(context).load(user.getImageUrl()).placeholder(R.drawable.icon_user).into(holder.image);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, ChatActivity.class).putExtra("uId", user.getUserId()).putExtra(  "name",user.getName()).putExtra("dp", user.getImageUrl()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UsersViewHolder extends RecyclerView.ViewHolder {

        TextView name, about;
        ImageView image;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.userName);
            about = itemView.findViewById(R.id.msg);
            image = itemView.findViewById(R.id.dp);
        }
    }
}
