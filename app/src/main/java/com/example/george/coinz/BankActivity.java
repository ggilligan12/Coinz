package com.example.george.coinz;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class BankActivity extends AppCompatActivity {

    private String tag = "BankActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Button backButton;

    private static Float shilDeposit;
    private static Float dolrDeposit;
    private static Float quidDeposit;
    private static Float penyDeposit;

    public static Float getShilDeposit() {return shilDeposit;}
    public static Float getDolrDeposit() {return dolrDeposit;}
    public static Float getQuidDeposit() {return quidDeposit;}
    public static Float getPenyDeposit() {return penyDeposit;}

    public static void setShilDeposit(Float d){BankActivity.shilDeposit = d;}
    public static void setDolrDeposit(Float d){BankActivity.dolrDeposit = d;}
    public static void setQuidDeposit(Float d){BankActivity.quidDeposit = d;}
    public static void setPenyDeposit(Float d){BankActivity.penyDeposit = d;}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank);

        backButton = findViewById(R.id.btn_bank_back);
        // Transferring to the main activity.
        backButton.setOnClickListener(v -> startActivity(new Intent(BankActivity.this, MainActivity.class)));

        String[] currency = new String[4];
        currency[0]="SHIL"; currency[1]="DOLR"; currency[2]="QUID"; currency[3]="PENY";

        //EditText shilBox = findViewById(R.id.SHIL_text);
        //EditText dolrBox = findViewById(R.id.DOLR_text);
        //EditText quidBox = findViewById(R.id.QUID_text);
        //EditText penyBox = findViewById(R.id.PENY_text);

        //EditText[] boxes = new EditText[4];
        //boxes[0]=shilBox; boxes[1]=dolrBox; boxes[2]=quidBox; boxes[3]=penyBox;

        //TextView collectedSHIL = findViewById(R.id.SHIL_text).setText()

        //CollectionReference users = db.collection("users");
        mAuth = FirebaseAuth.getInstance();
        String email = mAuth.getCurrentUser().getEmail();


        DocumentReference docRef = db.collection("users").document(email);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) { Log.d(tag, "DocumentSnapshot data: " + document.getData()); }
                else { Log.d(tag, "No such document"); }
            }
            else { Log.d(tag, "get failed with ", task.getException()); }
        });



    }

}
