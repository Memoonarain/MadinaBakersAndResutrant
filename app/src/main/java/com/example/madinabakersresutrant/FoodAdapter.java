package com.example.madinabakersresutrant;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    Context context;
    ArrayList<FoodModel> foodList;

    public FoodAdapter(Context context, ArrayList<FoodModel> foodList) {
        this.context = context;
        this.foodList = foodList;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.food_item, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodModel food = foodList.get(position);
        holder.foodName.setText(food.getFoodName());
        holder.foodPrice.setText(food.getPrice());

        Glide.with(context).load(food.getImg())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .fallback(R.drawable.placeholder)
                .into(holder.foodImage); // Add Glide dependency

        // Optional: set rating later if added in DB
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FoodItemDetailActivity.class);
            intent.putExtra("name", food.getFoodName());
            intent.putExtra("Id", food.getItemId());
            intent.putExtra("price", food.getPrice());
            intent.putExtra("image", food.getImg());
            intent.putExtra("category", food.getCategory());
            intent.putExtra("description", "Delicious food item made with love and spices.\nExperience the perfect fusion of traditional flavors and modern culinary craft with this mouth-watering dish. Carefully prepared using high-quality ingredients and authentic spices, every bite delivers a burst of taste that’s sure to satisfy your cravings. Whether you’re enjoying it as a snack, a meal, or sharing with loved ones, this food item is made with love and passion to ensure a truly unforgettable taste. Ideal for any occasion — hot, fresh, and full of flavor that lingers long after the last bite. One try is never enough!");
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {

        ImageView foodImage;
        TextView foodName, foodPrice;
        RatingBar foodRating;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);

            foodImage = itemView.findViewById(R.id.foodImage);
            foodName = itemView.findViewById(R.id.foodName);
            foodPrice = itemView.findViewById(R.id.foodPrice);
            foodRating = itemView.findViewById(R.id.foodRating);
        }
    }
}
