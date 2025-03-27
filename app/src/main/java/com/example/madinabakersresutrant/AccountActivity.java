package com.example.madinabakersresutrant;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AccountActivity extends AppCompatActivity {

    TextView birthdate, edtGenders;
    EditText edtName, edtEmail;
    ImageView imgProfile, edtBirthDateBtn, edtGenderBtn;
    Button btnSave;
    Uri imageUri;
    String uid;
    DatabaseReference userRef;
    StorageReference storageRef;
    LoadingDialogue loadingDialogue;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        loadingDialogue = new LoadingDialogue(AccountActivity.this);
        loadingDialogue.showLoadingDialog();
        // 🔌 Initialize
        birthdate = findViewById(R.id.birthdate);
        edtGenders = findViewById(R.id.Gender);
        edtName = findViewById(R.id.username);
        edtEmail = findViewById(R.id.email);
        edtBirthDateBtn = findViewById(R.id.edtBirthDate);
        edtGenderBtn = findViewById(R.id.edtGender);
        imgProfile = findViewById(R.id.user_dp);
        btnSave = findViewById(R.id.btnSave);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        storageRef = FirebaseStorage.getInstance().getReference("Food images/profile");

        // 👤 Load current data
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    loadingDialogue.hideLoadingDialog();
                    edtName.setText(snapshot.child("name").getValue(String.class));
                    edtEmail.setText(snapshot.child("email").getValue(String.class));
                    edtGenders.setText(snapshot.child("Gender").getValue(String.class));
                    birthdate.setText(convertMillisToDate(snapshot.child("birthdate").getValue(Long.class)));
                    ImageView UserImg = findViewById(R.id.user_dp);
                    String imgUri = snapshot.child("imgUrl").getValue(String.class);
                    Glide.with(AccountActivity.this).load(imgUri)
                            .placeholder(R.drawable.profile).
                            fallback(R.drawable.profile).
                            error(R.drawable.profile)
                            .into(UserImg);

                    TextView phoneView = findViewById(R.id.phoneNo);
                    phoneView.setText(snapshot.child("phone").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingDialogue.hideLoadingDialog();
                Toast.makeText(AccountActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });

        // 📅 Date Picker
        edtBirthDateBtn.setOnClickListener(v -> showDatePicker());

        // 🧑‍🎤 Gender Picker
        edtGenderBtn.setOnClickListener(v -> {
            String[] genders = {"Male", "Female", "Other"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Gender")
                    .setItems(genders, (dialog, which) -> edtGenders.setText(genders[which]));
            builder.show();
        });

        // 📸 Image Picker
        imgProfile.setOnClickListener(v -> selectImage());

        // 💾 Save Data
        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String gender = edtGenders.getText().toString().trim();
            String birthDateStr = birthdate.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || gender.isEmpty() || birthDateStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            loadingDialogue.showLoadingDialog();
            // Convert birthdate string to millis
            long birthMillis = convertDateToMillis(birthDateStr);

            if (imageUri != null) {
                // 🖼️ Upload image and then update all fields
                StorageReference fileRef = storageRef.child(uid + ".jpg");
                fileRef.putFile(imageUri)
                        .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imgUrl = uri.toString();
                            updateUserData(name, email, gender, birthMillis, imgUrl);
                        }))
                        .addOnFailureListener(e -> Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show());
            } else {
                // 📝 Only update text data (no image change)
                updateUserData(name, email, gender, birthMillis, null);
            }
        });
        findViewById(R.id.Logout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            finish();
        });
    }

    private void updateUserData(String name, String email, String gender, long birthMillis, String imgUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("email", email);
        updates.put("Gender", gender);
        updates.put("birthdate", birthMillis);
        if (imgUrl != null) updates.put("imgUrl", imgUrl);

        userRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                loadingDialogue.hideLoadingDialog();
            } else {
                Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show();
                loadingDialogue.hideLoadingDialog();
            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imgProfile.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, y, m, d) -> birthdate.setText(d + "/" + (m + 1) + "/" + y),
                year, month, day);
        dialog.show();
    }

    private long convertDateToMillis(String dateStr) {
        try {
            String[] parts = dateStr.split("/");
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[0]));
            cal.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1); // Month is 0-based
            cal.set(Calendar.YEAR, Integer.parseInt(parts[2]));
            return cal.getTimeInMillis();
        } catch (Exception e) {
            return 0;
        }
    }

    private String convertMillisToDate(Long millis) {
        if (millis == null) return "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1; // month is 0-based
        int year = calendar.get(Calendar.YEAR);
        return day + "/" + month + "/" + year;
    }
}
