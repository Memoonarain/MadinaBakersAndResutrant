package com.example.madinabakersresutrant;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    TextView totalTextView;
    Button checkoutButton;
    DatabaseReference dbRef;
    List<CartItem> cartItems;
    CartAdapter cartAdapter;
    String uid;
    private AlertDialog loadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        showLoadingDialog();
        recyclerView = findViewById(R.id.recyclerViewCartItems);
        totalTextView = findViewById(R.id.textViewTotal);
        checkoutButton = findViewById(R.id.buttonCheckout);
        //Setting Up cart Items
        cartItems = new ArrayList<>();
        cartAdapter = new CartAdapter(this, cartItems, this::updateTotal);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(cartAdapter);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user==null){Toast.makeText(this, "Please Sign in to Your Account First...", Toast.LENGTH_SHORT).show();finish();}
         uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (uid==null){Toast.makeText(this, "Please Sign in to Your Account First...", Toast.LENGTH_SHORT).show();finish();}
        loadCartItems();
        //CheckoutButton
        checkoutButton.setOnClickListener(v ->{
        if (cartItems.isEmpty()){
            Toast.makeText(this, "Please add some items to cart first..", Toast.LENGTH_SHORT).show();
            return;
        }
         showAddressBottomSheet();
        });
        //BottomNavigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_cart);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                // Using if-else instead of switch-case
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(CartActivity.this, MainActivity.class));
                    finish();
                    overridePendingTransition(0, 0);
                    return true;
                    // Switch to HomeActivity
                } else if (itemId == R.id.nav_cart) {
                } else if (itemId == R.id.nav_me) {
                    // Switch to ProfileActivity
                    startActivity(new Intent(CartActivity.this, ProfileActivity.class));
                    finish();
                    overridePendingTransition(0, 0);
                    return true;
                }

                return false;
            }
        });
    }

    private void loadCartItems() {
        dbRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("cart");
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartItems.clear();
                for (DataSnapshot itemSnap : snapshot.getChildren()) {
                    String id = itemSnap.getKey();
                    String name = itemSnap.child("name").getValue(String.class);
                    String imgUrl = itemSnap.child("img").getValue(String.class);
                    String category = itemSnap.child("category").getValue(String.class);
                    String Price = itemSnap.child("price").getValue(String.class);
                    String Quantity = String.valueOf(itemSnap.child("quantity").getValue()); // quantity might be Integer
                    if (Price == null || Quantity == null) continue;
                    int price = Integer.parseInt(Price.replace("Rs", "").trim());
                    int quantity = Integer.parseInt(Quantity);
                    cartItems.add(new CartItem(name, price, quantity, imgUrl));
                }
                hideLoadingDialog();
                cartAdapter.notifyDataSetChanged();updateTotal();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideLoadingDialog();
                Toast.makeText(CartActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updateTotal() {
        int total = 0;
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                total += item.getPrice() * item.getQuantity();
            }
        }
        totalTextView.setText( String.valueOf(total));
    }
    private void showAddressBottomSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_address, null);
        sheet.setContentView(view);
        sheet.show();

        RecyclerView recyclerSaved = view.findViewById(R.id.recyclerSavedAddresses);
        Button btnAdd = view.findViewById(R.id.btnAddNewAddress);
        Button btnPickup = view.findViewById(R.id.btnSelfPickup);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please Sign in to a account first...", Toast.LENGTH_SHORT).show();
            return;

        }

        DatabaseReference addressRef = FirebaseDatabase.getInstance().getReference("users")
                .child(user.getUid()).child("Addresses");

        List<AddressModel> addressList = new ArrayList<>();
        AddressAdapter addressAdapter = new AddressAdapter(this, addressList, selectedAddress -> {
            sheet.dismiss();
            showPaymentBottomSheet(selectedAddress.getAddress()); // or any detail you need
        });


        // Setup your adapter with already loaded addresses from Firebase
        recyclerSaved.setLayoutManager(new LinearLayoutManager(this));
        recyclerSaved.setAdapter(addressAdapter);
        addressRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                addressList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    AddressModel address = ds.getValue(AddressModel.class);
                    if (address != null) {
                        addressList.add(address);
                    }}
                addressAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Failed to load addresses", Toast.LENGTH_SHORT).show();
            }
        });
        btnAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddressActivity.class));
            sheet.dismiss();
        });

        btnPickup.setOnClickListener(v -> {
            sheet.dismiss();
            showPaymentBottomSheet("Self Pickup");
        });

    }
    private void showPaymentBottomSheet(String addressOrPickup) {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_payment, null);
        sheet.setContentView(view);
        sheet.show();

        TextView orderTotal = view.findViewById(R.id.tvOrderTotal);
        TextView delivery = view.findViewById(R.id.tvDeliveryCharges);
        TextView total = view.findViewById(R.id.tvFinalTotal);
        RadioGroup paymentOptions = view.findViewById(R.id.paymentOptionsGroup);
        Button btnPlace = view.findViewById(R.id.btnPlaceOrder);
        String orderPrice =totalTextView.getText().toString();
        int orderAmount =Integer.parseInt(orderPrice) ; // your existing method
        int deliveryCharges = addressOrPickup.equals("Self Pickup") ? 0 : 100;

        orderTotal.setText("Order Total: Rs. " + orderAmount);
        delivery.setText("Delivery Charges: Rs. " + deliveryCharges);
        total.setText("Total Payable: Rs. " + (orderAmount + deliveryCharges));

        btnPlace.setOnClickListener(v -> {
            int selectedId = paymentOptions.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
                return;
            }

            // Here, you can save the order to Firebase or wherever
            sheet.dismiss();
            showSuccessDialog();
        });
    }
    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Order Placed")
                .setMessage("Your order has been placed successfully!")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    finish(); // or go to Home screen
                })
                .show();
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
    public interface OnAddressClickListener {
        void onAddressClick(AddressModel addressModel);
    }

}