package com.example.madinabakersresutrant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Firebase;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class AccountSetup extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String verificationId;
    private EditText edtName, edtPhone, edtOtp, edtAddress;
    private Button btnOtp, btnNext;
    LoadingDialogue loadingDialogue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_setup);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        loadingDialogue = new LoadingDialogue(AccountSetup.this);
        mAuth = FirebaseAuth.getInstance();
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtOtp = findViewById(R.id.edtOtp);
        btnOtp = findViewById(R.id.btnOtp);
        btnNext = findViewById(R.id.btnNext);

        findViewById(R.id.txtSkip).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        btnOtp.setOnClickListener(v -> generateOtp());

        btnNext.setOnClickListener(v -> {
            validateAndProceed();
            loadingDialogue.showLoadingDialog();
        });
    }

    private void generateOtp() {
        String phoneNumber = edtPhone.getText().toString().trim();

        if (TextUtils.isEmpty(phoneNumber) || phoneNumber.length() < 10) {
            Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        loadingDialogue.showLoadingDialog();
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(PhoneAuthCredential credential) {
                                // Auto-retrieval or instant verification succeeded
                                signInWithPhoneAuthCredential(credential);
                                loadingDialogue.hideLoadingDialog();
                            }

                            @Override
                            public void onVerificationFailed(FirebaseException e) {
                                Toast.makeText(AccountSetup.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                loadingDialogue.hideLoadingDialog();
                            }

                            @Override
                            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                                // Save verification ID to use in OTP verification
                                AccountSetup.this.verificationId = verificationId;
                                Toast.makeText(AccountSetup.this, "OTP sent", Toast.LENGTH_SHORT).show();
                                loadingDialogue.hideLoadingDialog();
                            }
                        })
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
        // Add code to generate OTP here
    }

    private void validateAndProceed() {
        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String otp = edtOtp.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(otp) ) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (verificationId == null) {
            Toast.makeText(this, "Verification ID is missing. Please request OTP again.", Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithPhoneAuthCredential(credential);

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign-in succeeded, proceed with the user
                        Toast.makeText(AccountSetup.this, "Phone verified", Toast.LENGTH_SHORT).show();
                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid);
                        HashMap<String, String> userMap = new HashMap<>();
                        userMap.put("name", edtName.getText().toString());
                        userMap.put("phone", edtPhone.getText().toString().trim());
                        ref.setValue(userMap).addOnCompleteListener(t -> {
                            if (t.isSuccessful()) {
                                Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(AccountSetup.this, MainActivity.class));
                                loadingDialogue.hideLoadingDialog();
                                finish();
                            } else {
                                Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show();
                                loadingDialogue.hideLoadingDialog();
                            }
                        });
                    } else {
                        loadingDialogue.hideLoadingDialog();
                        Toast.makeText(AccountSetup.this, "Verification failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
