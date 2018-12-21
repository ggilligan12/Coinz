package com.example.george.coinz;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class ItemsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);


        Button backButton = findViewById(R.id.btn_items_back);

        // Transferring to the main activity.
        backButton.setOnClickListener(v -> finish());

    }

}
