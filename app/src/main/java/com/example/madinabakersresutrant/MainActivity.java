package com.example.madinabakersresutrant;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<FoodModel> foodList;
    FoodAdapter foodAdapter;
    DatabaseReference dbRef;
    private AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.food_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        foodList = new ArrayList<>();
        foodAdapter = new FoodAdapter(this, foodList);
        recyclerView.setAdapter(foodAdapter);

        setupBottomNav();
        setupCategoryButtons(); // <--- Setup your category buttons (e.g., All, Pizza, etc.)
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_cart) {
                startActivity(new Intent(MainActivity.this, CartActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_me) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void setupCategoryButtons() {
        findViewById(R.id.allfood).setOnClickListener(v -> {fetchFoodItemsByCategory("All");
            highlightSelectedButton(v);});
        findViewById(R.id.burger).setOnClickListener(v -> {fetchFoodItemsByCategory("burger");
            highlightSelectedButton(v);});
        findViewById(R.id.drinks).setOnClickListener(v -> {fetchFoodItemsByCategory("drink");
            highlightSelectedButton(v);});
        findViewById(R.id.otherfood).setOnClickListener(v -> {fetchFoodItemsByCategory("other");
            highlightSelectedButton(v);});
        findViewById(R.id.pizza).setOnClickListener(v -> {fetchFoodItemsByCategory("pizza");
            highlightSelectedButton(v);});
        findViewById(R.id.shwarma).setOnClickListener(v -> {fetchFoodItemsByCategory("shawarma");
            highlightSelectedButton(v);});
        findViewById(R.id.sweets).setOnClickListener(v -> {fetchFoodItemsByCategory("sweet");
            highlightSelectedButton(v);});
        // Add other categories similarly...
    }

    private void highlightSelectedButton(View selectedButton) {
        int[] buttonIds = {
                R.id.allfood, R.id.pizza, R.id.burger,
                R.id.sweets, R.id.drinks, R.id.otherfood, R.id.shwarma
        };

        for (int id : buttonIds) {
            TextView button = findViewById(id);
            button.setBackgroundResource(R.drawable.category_background);
            button.setTextColor(getResources().getColor(R.color.white)); // or use ContextCompat
        }

        TextView selectedBtn = (TextView) selectedButton;
        selectedBtn.setBackgroundResource(R.drawable.search_bar_background);
        selectedBtn.setTextColor(getResources().getColor(R.color.black));
    }


    public void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false); // disable dismiss on outside touch

        // Create a LinearLayout with a ProgressBar and TextView
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(40, 40, 40, 40);
        layout.setGravity(Gravity.CENTER_VERTICAL);

        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);

        TextView loadingText = new TextView(this);
        loadingText.setText("Loading...");
        loadingText.setTextSize(18);
        loadingText.setPadding(30, 0, 0, 0);

        layout.addView(progressBar);
        layout.addView(loadingText);

        builder.setView(layout);
        loadingDialog = builder.create();
        loadingDialog.show();
    }

    public void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void fetchFoodItemsByCategory(String selectedCategory) {
        showLoadingDialog(); // <--- Show loader
        dbRef = FirebaseDatabase.getInstance().getReference("food_categories");
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                foodList.clear();

                for (DataSnapshot categorySnap : snapshot.getChildren()) {
                    for (DataSnapshot foodSnap : categorySnap.getChildren()) {
                        String name = foodSnap.getKey();
                        String id = foodSnap.child("ItemId").getValue(String.class);
                        String price = foodSnap.child("Price").getValue(String.class);
                        String category = foodSnap.child("category").getValue(String.class);
                        String img = foodSnap.child("img").getValue(String.class);

                        if (selectedCategory.toLowerCase().equals(category.toLowerCase()) || selectedCategory.equals("All") ) {
                            foodList.add(new FoodModel(name, price, category, img, id));
                        }
                    }
                }

                foodAdapter.notifyDataSetChanged();
                hideLoadingDialog(); // <--- Hide loader

                if (foodList.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No items found in " + selectedCategory + "!", Toast.LENGTH_SHORT).show();
                    recyclerView.setVisibility(View.GONE);
                    findViewById(R.id.popularFood).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.popularFood).setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideLoadingDialog(); // <--- Hide loader
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
