package com.example.george.coinz;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MailActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private DocumentReference firestoreChat;

    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        backButton = findViewById(R.id.btn_mail_back);
        // Transferring to the main activity.
        backButton.setOnClickListener(v -> startActivity(new Intent(MailActivity.this, MainActivity.class)));



    }

}
