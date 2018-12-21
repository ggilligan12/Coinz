package com.example.george.coinz;

//import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.view.View;
import android.widget.Button;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        Button logoutButton = findViewById(R.id.btn_logout);

        // Transferring to the main activity.
        logoutButton.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, MainActivity.class)));

    }



}
