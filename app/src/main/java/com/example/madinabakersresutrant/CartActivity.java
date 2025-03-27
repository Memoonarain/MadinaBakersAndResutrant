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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
    LoadingDialogue loadingDialogues;


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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadingDialogues =new LoadingDialogue(CartActivity.this);
        loadingDialogues.showLoadingDialog();
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
                    cartItems.add(new CartItem(name, price, quantity, imgUrl,id));
                }
                loadingDialogues.hideLoadingDialog();
                cartAdapter.notifyDataSetChanged();updateTotal();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingDialogues.hideLoadingDialog();
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

            RadioButton selectedRadio = view.findViewById(selectedId);
            String paymentMethod = selectedRadio.getText().toString();
            String address = addressOrPickup.equals("Self Pickup") ? "N/A" : addressOrPickup;
            String deliveryType = addressOrPickup.equals("Self Pickup") ? "Self Pickup" : "Home Delivery";

            if (paymentMethod.equals("Cash on Delivery")){
                // Call the function with all required arguments
                placeOrder(deliveryType, address, paymentMethod);
                sheet.dismiss();
            }
            else {
                Toast.makeText(this, "Online payment is not available for now...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void placeOrder(String deliveryType, String address, String paymentMethod) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login", Toast.LENGTH_SHORT).show();
            return;
        }

        String orderId = FirebaseDatabase.getInstance().getReference().push().getKey();
        long timestamp = System.currentTimeMillis();

        List<CartItem> selectedItems = new ArrayList<>();
        int total = 0;

        // Filter for selected items and calculate total price based on updated quantities
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                selectedItems.add(item);
                total += item.getPrice() * item.getQuantity();
            }
        }

        // Ensure that selectedItems is not empty (user has selected items)
        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Please select items to place an order", Toast.LENGTH_SHORT).show();
            return;
        }
        loadingDialogues.showLoadingDialog();
        // Create the OrderModel with only selected items
        OrderModel order = new OrderModel(
                orderId,
                user.getUid(),
                "Pending",
                selectedItems,  // Use selected items list
                total,
                paymentMethod,
                deliveryType,
                address,
                timestamp
        );

        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("Orders").child(uid);
        orderRef.child(orderId).setValue(order).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Order placed!", Toast.LENGTH_SHORT).show();
                loadingDialogues.hideLoadingDialog();
                for (CartItem item : selectedItems) {
                    dbRef.child(item.getItemId()).removeValue(); // or use unique ID instead of name
                }

                startActivity(new Intent(CartActivity.this, OrdersActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Failed to place order", Toast.LENGTH_SHORT).show();
                loadingDialogues.hideLoadingDialog();
            }
        });

        // Store the order under the user's "MyOrders"
        FirebaseDatabase.getInstance().getReference("users")
                .child(user.getUid())
                .child("MyOrders")
                .child(orderId)
                .setValue(order);
    }

    public interface OnAddressClickListener {
        void onAddressClick(AddressModel addressModel);
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
            startActivity(new Intent(CartActivity.this, AccountActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}