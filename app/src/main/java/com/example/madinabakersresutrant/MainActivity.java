package com.example.madinabakersresutrant;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
    LoadingDialogue loadingDialogues;
    EditText searchEdt;
    ImageView searchIcon;
    LinearLayout searchLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        searchEdt = findViewById(R.id.searchBar);
        searchLayout= findViewById(R.id.searchLayout);
        searchIcon = findViewById(R.id.search_icon);
        loadingDialogues =new LoadingDialogue(MainActivity.this);
        recyclerView = findViewById(R.id.food_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        foodList = new ArrayList<>();
        foodAdapter = new FoodAdapter(this, foodList);
        recyclerView.setAdapter(foodAdapter);
        searchIcon.setOnClickListener(v -> {
            String query = searchEdt.getText().toString().trim().toLowerCase();
            if (!query.isEmpty()) {
                searchFoodItems(query);
            } else {
                Toast.makeText(MainActivity.this, "Enter a food name to search", Toast.LENGTH_SHORT).show();
            }
        });

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

    private void searchFoodItems(String query) {
        loadingDialogues.showLoadingDialog();
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

                        if (name != null && name.toLowerCase().contains(query)) {
                            foodList.add(new FoodModel(name, price, category, img, id));
                        }
                    }
                }

                foodAdapter.notifyDataSetChanged();
                loadingDialogues.hideLoadingDialog();

                if (foodList.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No items found for: " + query, Toast.LENGTH_SHORT).show();
                    recyclerView.setVisibility(View.GONE);
                    findViewById(R.id.popularFood).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.popularFood).setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingDialogues.hideLoadingDialog();
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            // Open AccountActivity
            startActivity(new Intent(MainActivity.this, AccountActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchFoodItemsByCategory(String selectedCategory) {
        loadingDialogues.showLoadingDialog();
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
                loadingDialogues.hideLoadingDialog();

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
                loadingDialogues.hideLoadingDialog();
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
