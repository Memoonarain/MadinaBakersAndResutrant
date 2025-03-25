package com.example.talkbox;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Splashactivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    ProgressDialog pd;
    private static final long SPLASH_DELAY = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splashactivity);

        firebaseAuth = FirebaseAuth.getInstance();

        // Apply window insets listener
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Delay the transition to the MainActivity or LoginActivity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                Intent intent;
                if (currentUser != null) {
                    // User is logged in, navigate to MainActivity
                    intent = new Intent(Splashactivity.this, MainActivity.class);
                } else {
                    // User is not logged in, navigate to LoginActivity
                    intent = new Intent(Splashactivity.this, LoginActivity.class);
                }
                startActivity(intent);
                finish(); // Close the Splashactivity so the user can't return to it
            }
        }, SPLASH_DELAY);
    }
}
