package com.example.madinabakersresutrant;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddressActivity extends AppCompatActivity {
    private RecyclerView addressListView;
    List<AddressModel> addressModelList;
    AddressAdapter addressAdapter;
    LoadingDialogue loadingDialogue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_address);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        loadingDialogue = new LoadingDialogue(this);
        addressModelList = new ArrayList<>();
        addressAdapter = new AddressAdapter(this, addressModelList);
        addressListView = findViewById(R.id.addressListView);
        fetchUserAddresses();

        findViewById(R.id.btnAddNewAddress).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Choose Address Option")
                    .setItems(new String[]{"Enter Manually", "Pick on Map"}, (dialog, which) -> {
                        if (which == 0) {

                            // Open Manual Address Input Activity or Dialog
                            showManualAddressDialog();

                        } else {
                            Toast.makeText(this, "Coming Soon...", Toast.LENGTH_SHORT).show();
                            // Open MapActivity to pick location
//                            Intent i = new Intent(this, MapActivity.class);
//                            startActivityForResult(i, 101);

                        }
                    }).show();

        });
    }
    private void showManualAddressDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_address, null);

        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etPhone = dialogView.findViewById(R.id.etPhone);
        EditText etAddress = dialogView.findViewById(R.id.etAddress);
        Spinner spinnerCity = dialogView.findViewById(R.id.spinnerCity);

// List of available cities
        String[] cities = {"LiaquatPur"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cities);
        spinnerCity.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Address")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {

                    String name = etName.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();
                    String city = spinnerCity.getSelectedItem().toString();
                    if (city.equals("Select City")) {
                        Toast.makeText(this, "Please select a city", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String address = etAddress.getText().toString().trim();

                    if (name.isEmpty() || phone.isEmpty() || city.isEmpty() || address.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    loadingDialogue.showLoadingDialog();
                    // Firebase Upload
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users")
                            .child(userId).child("Addresses");

                    String addressId = ref.push().getKey();

                    Map<String, Object> map = new HashMap<>();
                    map.put("id", addressId);
                    map.put("name", name);
                    map.put("phone", phone);
                    map.put("city", city);
                    map.put("address", address);

                    ref.child(addressId).setValue(map).addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Address Saved", Toast.LENGTH_SHORT).show();
                        loadingDialogue.hideLoadingDialog();
                        fetchUserAddresses();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        loadingDialogue.hideLoadingDialog();
                    });

                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void fetchUserAddresses() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please Sign in to a account first...", Toast.LENGTH_SHORT).show();
            return;

        }

        DatabaseReference addressRef = FirebaseDatabase.getInstance().getReference("users")
                .child(user.getUid()).child("Addresses");

        List<AddressModel> addressList = new ArrayList<>();
        addressAdapter = new AddressAdapter(this, addressList, new AddressAdapter.OnAddressActionListener() {
            @Override
            public void onEdit(AddressModel model) {
                showEditAddressDialog(model);
            }

            @Override
            public void onDelete(AddressModel model) {
                deleteAddressFromFirebase(model);
            }
        });

        addressListView.setLayoutManager(new LinearLayoutManager(this));
        addressListView.setAdapter(addressAdapter);

        addressRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                addressList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    AddressModel address = ds.getValue(AddressModel.class);
                    if (address != null) {
                        addressList.add(address);
                }}
                if (addressList.isEmpty()) {
                    loadingDialogue.hideLoadingDialog();
                    addressListView.setVisibility(View.GONE);
                    findViewById(R.id.txtNoAddress).setVisibility(View.VISIBLE);
                } else {
                    loadingDialogue.hideLoadingDialog();
                    addressListView.setVisibility(View.VISIBLE);
                    findViewById(R.id.txtNoAddress).setVisibility(View.GONE);
                }
                addressAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingDialogue.hideLoadingDialog();
                Toast.makeText(getApplicationContext(), "Failed to load addresses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditAddressDialog(AddressModel addressModel) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_address, null);

        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etPhone = dialogView.findViewById(R.id.etPhone);
        EditText etAddress = dialogView.findViewById(R.id.etAddress);
        Spinner spinnerCity = dialogView.findViewById(R.id.spinnerCity);

        String[] cities = {"LiaquatPur"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cities);
        spinnerCity.setAdapter(adapter);

        // Prefill values
        etName.setText(addressModel.getName());
        etPhone.setText(addressModel.getPhone());
        etAddress.setText(addressModel.getAddress());

        // Set spinner selection
        for (int i = 0; i < cities.length; i++) {
            if (cities[i].equals(addressModel.getCity())) {
                spinnerCity.setSelection(i);
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Edit Address")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();
                    String city = spinnerCity.getSelectedItem().toString();
                    String address = etAddress.getText().toString().trim();

                    if (name.isEmpty() || phone.isEmpty() || city.isEmpty() || address.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users")
                            .child(FirebaseAuth.getInstance().getUid())
                            .child("Addresses")
                            .child(addressModel.getId());

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("name", name);
                    updates.put("phone", phone);
                    updates.put("city", city);
                    updates.put("address", address);

                    ref.updateChildren(updates)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Address Updated", Toast.LENGTH_SHORT).show();
                                fetchUserAddresses();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void deleteAddressFromFirebase(AddressModel addressModel) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users")
                .child(FirebaseAuth.getInstance().getUid())
                .child("Addresses")
                .child(addressModel.getId());

        new AlertDialog.Builder(this)
                .setTitle("Delete Address")
                .setMessage("Are you sure you want to delete this address?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    ref.removeValue().addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Address Deleted", Toast.LENGTH_SHORT).show();
                        fetchUserAddresses();
                    }).addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
                    );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            String address = data.getStringExtra("selected_address");
            double lat = data.getDoubleExtra("latitude", 0);
            double lng = data.getDoubleExtra("longitude", 0);

            // Now save this to Firebase here or show to user
        }
    }

}