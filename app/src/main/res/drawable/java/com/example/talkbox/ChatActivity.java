package com.example.talkbox;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.talkbox.adapters.MessagesAdapter;
import com.example.talkbox.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {
    String uId, name, dp, senderRoom, receiverRoom;
    ImageView profilePic, sendBtn;
    Toolbar toolbar;
    TextView userName;
    EditText messageEditText;
    RecyclerView chatRecyclerView;
    MessagesAdapter messagesAdapter;
    ArrayList<Message> messages;
    DatabaseReference chatReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.primaryColor));
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        uId = getIntent().getStringExtra("uId");
        name = getIntent().getStringExtra("name");
        dp = getIntent().getStringExtra("dp");

        profilePic = findViewById(R.id.dp);
        userName = findViewById(R.id.userName);
        sendBtn = findViewById(R.id.sendbtn);
        messageEditText = findViewById(R.id.edmsg);
        chatRecyclerView = findViewById(R.id.chatRView);

        userName.setText(name);
        Glide.with(ChatActivity.this).load(dp).placeholder(R.drawable.icon_user).into(profilePic);

        String senderUid = FirebaseAuth.getInstance().getUid();
        String receiverUid = uId;
        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        messages = new ArrayList<>();
        messagesAdapter = new MessagesAdapter(this, messages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(messagesAdapter);

        chatReference = FirebaseDatabase.getInstance("https://talk-box-829922-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("chats");

        sendBtn.setOnClickListener(v -> {
            String messageText = messageEditText.getText().toString();
            if (!messageText.isEmpty()) {
                long timestamp = System.currentTimeMillis();
                Message message = new Message(messageText, senderUid, timestamp);
                messageEditText.setText("");

                chatReference.child(senderRoom).push().setValue(message).addOnSuccessListener(aVoid ->
                        chatReference.child(receiverRoom).push().setValue(message)
                );
            }
        });

        loadMessages();
    }

    private void loadMessages() {
        chatReference.child(senderRoom).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Message message = snapshot1.getValue(Message.class);
                    messages.add(message);
                }
                messagesAdapter.notifyDataSetChanged();
                chatRecyclerView.scrollToPosition(messages.size() - 1);
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.chat_toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
