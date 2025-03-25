package com.example.talkbox;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.talkbox.UsersAdapter;
import com.example.talkbox.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    RecyclerView usersList;
    Toolbar toolbar;
    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    UsersAdapter usersAdapter;
    ArrayList<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        usersList = findViewById(R.id.rvChatList);
        setSupportActionBar(toolbar);
        firebaseAuth = FirebaseAuth.getInstance();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.primaryColor));

        // Initialize the users list and adapter
        users = new ArrayList<>();
        usersAdapter = new UsersAdapter(this, users);
        usersList.setLayoutManager(new LinearLayoutManager(this));
        usersList.setAdapter(usersAdapter);

        // Setup Firebase database reference
        databaseReference = FirebaseDatabase.getInstance("https://talk-box-829922-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("users");
        fetchUsers();

        Log.d(TAG, "MainActivity created successfully");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.newChat) {
            // Handle new chat action
        } else if (item.getItemId() == R.id.Logout) {
            firebaseAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        } else if (item.getItemId() == R.id.Settings) {
            // Handle settings action
        } else if (item.getItemId() == R.id.search) {
            // Handle search action
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchUsers() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    User User = snapshot1.getValue(User.class);
                    if (User != null) {
                        users.add(User);
                    }
                }
                usersAdapter.notifyDataSetChanged();
                Log.d(TAG, "Users fetched and adapter notified");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }
}
