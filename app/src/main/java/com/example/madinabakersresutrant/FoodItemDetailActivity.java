package com.example.madinabakersresutrant;

import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class FoodItemDetailActivity extends AppCompatActivity {

    ImageView foodImage;
    TextView foodName, foodPrice, foodDescription, foodReviews;
    Button addToCartBtn;
    EditText quantityEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_item_detail);
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.brown));

        foodImage = findViewById(R.id.foodImage);
        foodName = findViewById(R.id.foodName);
        foodPrice = findViewById(R.id.foodPrice);
        foodDescription = findViewById(R.id.foodDescription);
//        foodReviews = findViewById(R.id.foodReviews);
        addToCartBtn = findViewById(R.id.addToCartBtn);
        quantityEditText = findViewById(R.id.itemQuantity);
        // Get data from intent
        String itemId = getIntent().getStringExtra("Id");
        String name = getIntent().getStringExtra("name");
        String price = getIntent().getStringExtra("price");
        String image = getIntent().getStringExtra("image");
        String category = getIntent().getStringExtra("category");
        String description = getIntent().getStringExtra("description"); // Optional

        // Set values
        foodName.setText(name);
        foodPrice.setText(price);
        foodDescription.setText(description != null ? description : "No description available.");
        // Load image (use Glide or Picasso)
        Glide.with(this).load(image)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .fallback(R.drawable.placeholder)
                .into(foodImage);
        findViewById(R.id.increaseNo).setOnClickListener(v -> {
            String quantityStr = quantityEditText.getText().toString().trim();

            if (quantityStr.isEmpty()) {
                Toast.makeText(this, "Enter quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            int quantity = Integer.parseInt(quantityStr);
            quantity++;
            quantityEditText.setText(String.valueOf(quantity));  // ← Corrected here
        });
        findViewById(R.id.decreaseNo).setOnClickListener(v -> {
            String quantityStr = quantityEditText.getText().toString().trim();

            if (quantityStr.isEmpty()) {
                Toast.makeText(this, "Enter quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            int quantity = Integer.parseInt(quantityStr);
            quantity--;
            quantityEditText.setText(String.valueOf(quantity));  // ← Corrected here
        });

        // Add to cart click
        addToCartBtn.setOnClickListener(v -> {
            addToCart(itemId,name,price,image,category);
        });
    }
    private void addToCart(String itemId, String name, String price, String imgUrl, String category) {
        String quantityStr = quantityEditText.getText().toString().trim();

        if (quantityStr.isEmpty()) {
            Toast.makeText(this, "Enter quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity = Integer.parseInt(quantityStr);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();

        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("cart")
                .child(itemId); // use itemId to avoid duplicates

        HashMap<String, Object> cartItem = new HashMap<>();
        cartItem.put("name", name);
        cartItem.put("price", price);
        cartItem.put("img", imgUrl);
        cartItem.put("category", category);
        cartItem.put("quantity", quantity);

        cartRef.setValue(cartItem).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Item added to cart", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
