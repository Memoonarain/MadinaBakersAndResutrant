package com.example.madinabakersresutrant;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class OrdersActivity extends AppCompatActivity {

    LinearLayout allOrders, toReceive, toShip, toPay, cancelled, toReview,pending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_orders);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        allOrders = findViewById(R.id.all_orders);
        toReceive = findViewById(R.id.to_recieve);
        toShip = findViewById(R.id.to_ship);
        toPay = findViewById(R.id.to_pay);
        cancelled = findViewById(R.id.cancelled);
        toReview = findViewById(R.id.to_review);

        allOrders.setOnClickListener(v -> openFilteredOrders("All"));
        toReceive.setOnClickListener(v -> openFilteredOrders("To Recieve"));
        toShip.setOnClickListener(v -> openFilteredOrders("To Ship"));
        toPay.setOnClickListener(v -> openFilteredOrders("Pending"));
        cancelled.setOnClickListener(v -> openFilteredOrders("Cancelled"));
        toReview.setOnClickListener(v -> openFilteredOrders("To Review"));
    }

    private void openFilteredOrders(String filterType) {
        Intent intent = new Intent(this, FilteredOrdersActivity.class);
        intent.putExtra("status", filterType);
        startActivity(intent);
    }
}