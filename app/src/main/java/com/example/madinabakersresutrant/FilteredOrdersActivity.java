package com.example.madinabakersresutrant;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FilteredOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ArrayList<OrderModel> orderList;
    private OrderAdapter orderAdapter;
    private String filterStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtered_orders);

        recyclerView = findViewById(R.id.filtered_orders_recycler);
        progressBar = findViewById(R.id.filtered_orders_progress);

        filterStatus = getIntent().getStringExtra("status");
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter( this,orderList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(orderAdapter);

        loadFilteredOrders();
    }

    private void loadFilteredOrders() {
        progressBar.setVisibility(View.VISIBLE);
        String uid = FirebaseAuth.getInstance().getUid();
        FirebaseDatabase.getInstance().getReference("Orders").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderList.clear();
                        for (DataSnapshot orderSnap : snapshot.getChildren()) {
                            OrderModel order = orderSnap.getValue(OrderModel.class);
                            Log.e("OrderCheck", "Order ID: " + order.getOrderId() + " Status: " + order.getStatus());

                            // ✅ FIX: Add all if filter is "All"
                            if (filterStatus.equalsIgnoreCase("All") ||
                                    order.getStatus().equalsIgnoreCase(filterStatus)) {
                                orderList.add(order);
                            }
                        }

                        orderAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);

                        if (orderList.isEmpty()) {
                            Toast.makeText(FilteredOrdersActivity.this, "No orders found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(FilteredOrdersActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
