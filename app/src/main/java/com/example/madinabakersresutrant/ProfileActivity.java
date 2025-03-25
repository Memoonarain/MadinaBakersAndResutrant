package com.example.madinabakersresutrant;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    String uid;
    DatabaseReference userRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                   TextView UserName = findViewById(R.id.txtUserName);
                   UserName.setText(snapshot.child("name").getValue(String.class));
                    ImageView UserImg = findViewById(R.id.img_user_dp);
                    String imgUri = snapshot.child("imgUrl").getValue(String.class);
                    Glide.with(ProfileActivity.this).load(imgUri)
                            .placeholder(R.drawable.profile).
                            fallback(R.drawable.profile).
                            error(R.drawable.profile)
                            .into(UserImg);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.AccountSection).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, AccountActivity.class));
        });
        findViewById(R.id.addressSection).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, AddressActivity.class));
        });

        findViewById(R.id.Logout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            finish();
        });
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_me);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                // Using if-else instead of switch-case
                if (itemId == R.id.nav_home) {
                    // Switch to HomeActivity
                    startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                    finish();
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_cart) {
                    startActivity(new Intent(ProfileActivity.this, CartActivity.class));
                    finish();
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_me) {
                    // Switch to ProfileActivity
                }

                return false;
            }
        });
    }
}