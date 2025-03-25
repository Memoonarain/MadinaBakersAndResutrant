package com.example.talkbox;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.talkbox.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";
    private FirebaseAuth auth;
    private ProgressDialog dialog;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private Uri selectedImg;

    private ImageView imgAddUser, imgUser;
    private EditText edtName, edtAbout;
    private Button nextButton;

    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://talk-box-829922-default-rtdb.asia-southeast1.firebasedatabase.app");
        storage = FirebaseStorage.getInstance();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Setting Up Your Profile...");
        dialog.setCancelable(false);


        imgAddUser = findViewById(R.id.imgEdit);
        imgUser = findViewById(R.id.userImg);
        edtName = findViewById(R.id.edtUserName);
        edtAbout = findViewById(R.id.edtUserAbout); // Add about section
        nextButton = findViewById(R.id.btnCreateAccount);

        phoneNumber = auth.getCurrentUser().getPhoneNumber();
        if (phoneNumber == null) {
            // If phone number is not available in FirebaseAuth, get it from the intent
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra("phoneNumber")) {
                phoneNumber = intent.getStringExtra("phoneNumber");
            }
        }

        Log.d(TAG, "Phone number: " + phoneNumber);

        imgAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent().setAction(Intent.ACTION_GET_CONTENT).setType("image/*"), 45);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edtName.getText().toString().isEmpty()) {
                    edtName.setError("Please type a Name...");
                    return;
                }
                if (edtAbout.getText().toString().isEmpty()) {
                    edtAbout.setError("Please type something about yourself...");
                    return;
                }
                dialog.show();
                if (selectedImg != null) {
                    uploadProfileImage();
                } else {
                    saveUserProfile(null);
                }
            }
        });
    }

    private void uploadProfileImage() {
        StorageReference reference = storage.getReference().child("Profiles").child(auth.getUid());
        reference.putFile(selectedImg).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imageUrl = uri.toString();
                            saveUserProfile(imageUrl);
                        }
                    });
                } else {
                    dialog.dismiss();
                    Toast.makeText(SignupActivity.this, "Image upload failed" +reference, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveUserProfile(String imageUrl) {
        String uId = auth.getUid();
        String name = edtName.getText().toString();
        String about = edtAbout.getText().toString();

        if (imageUrl == null) {
            imageUrl = "no image"; // Placeholder for no image
        }

        User user = new User(phoneNumber,imageUrl,uId,  name, about ); // Adjust user model to include about
        database.getReference().child("users").child(phoneNumber).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialog.dismiss();
                if (task.isSuccessful()) {
                    startActivity(new Intent(SignupActivity.this, MainActivity.class));
                    finish();
                    Toast.makeText(SignupActivity.this, "Profile setup successful", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SignupActivity.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getData() != null) {
            imgUser.setImageURI(data.getData());
            selectedImg = data.getData();
        }
    }
}
