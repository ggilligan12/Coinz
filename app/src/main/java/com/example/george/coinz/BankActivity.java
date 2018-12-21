package com.example.george.coinz;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

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

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank);

        backButton = findViewById(R.id.btn_bank_back);
        // Transferring to the main activity.
        backButton.setOnClickListener(v -> finish());

        //String[] currency = new String[4];
        //currency[0]="SHIL"; currency[1]="DOLR"; currency[2]="QUID"; currency[3]="PENY";

        EditText shilBox = findViewById(R.id.SHIL_text);
        EditText dolrBox = findViewById(R.id.DOLR_text);
        EditText quidBox = findViewById(R.id.QUID_text);
        EditText penyBox = findViewById(R.id.PENY_text);

        EditText[] boxes = new EditText[4];
        boxes[0]=shilBox; boxes[1]=dolrBox; boxes[2]=quidBox; boxes[3]=penyBox;


        TextView shils = findViewById(R.id.shils);
        shils.setText("From "+MainActivity.getWallet().get(0).toString()+" SHIL");
        TextView dolrs = findViewById(R.id.dolrs);
        dolrs.setText("From "+MainActivity.getWallet().get(1).toString()+" DOLR");
        TextView quids = findViewById(R.id.quids);
        quids.setText("From "+MainActivity.getWallet().get(2).toString()+" QUID");
        TextView penys = findViewById(R.id.penys);
        penys.setText("From "+MainActivity.getWallet().get(3).toString()+" PENY");


        Button pocket = findViewById(R.id.pocket);
        pocket.setOnClickListener(v -> {
            Float z = Float.parseFloat("0.0");
            ArrayList<Float> bxam = new ArrayList<>();
            for(EditText b : boxes){
                if (b.getText().toString().isEmpty()) {
                    bxam.add(z);
                } else {
                    bxam.add(Float.parseFloat(b.getText().toString()));
                }
            }
            Float shil = bxam.get(0);
            Float dolr = bxam.get(1);
            Float quid = bxam.get(2);
            Float peny = bxam.get(3);

            Log.d(tag, "on click check" + shil +dolr + quid+ peny);
            boolean overdeposit = (shil+dolr+quid+peny>25);
            boolean over25 = (shil+dolr+quid+peny)<=25;
            boolean nothing_entered = shil.equals(z) && dolr.equals(z) && quid.equals(z) && peny.equals(z);
            boolean negative_deposit = shil> MainActivity.getWallet().get(0) || dolr > MainActivity.getWallet().get(1)
                    || quid > MainActivity.getWallet().get(2) || peny > MainActivity.getWallet().get(3);

            Log.d(tag, "testy"+shil+dolr+quid+peny);

            if (nothing_entered){
                Toast.makeText(BankActivity.this , "Cannot deposit nothing",Toast.LENGTH_LONG).show();
                Log.d(tag, "preforming 0 task");
            }
            else if (negative_deposit){
                Toast.makeText(BankActivity.this , "Nice try \n you can't deposit more than you have",Toast.LENGTH_LONG).show();
                Log.d(tag, "preforming 1 task");
            }
            else if(!over25 ){
                Toast.makeText(BankActivity.this , "You can only deposit 25 coinz in one day",Toast.LENGTH_LONG).show();
                Log.d(tag, "preforming 3 task");
            }
            else if(overdeposit){
                Toast.makeText(BankActivity.this , "This deposit will put you over your 25 coin limit \n try again",Toast.LENGTH_LONG).show();
                Log.d(tag, "preforming 3 task");
            }
            else if (!negative_deposit && !nothing_entered) {
                setShilDeposit(shil); setDolrDeposit(dolr); setQuidDeposit(quid); setPenyDeposit(peny);
                Log.d(tag, "on click check" + getShilDeposit() +getDolrDeposit() + getQuidDeposit()+ getPenyDeposit());
                //startActivity(new Intent(BankActivity.this, BankPopUp.class));
                Log.d(tag, "preforming 2 task");
            }
            else { Log.d(tag, "LEEK");}
        });
        //CollectionReference users = db.collection("users");
        //mAuth = FirebaseAuth.getInstance();
        //String email = mAuth.getCurrentUser().getEmail();


        /*DocumentReference docRef = db.collection("users").document(email);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d(tag, "DocumentSnapshot data: " + document.getData());

                }
                else { Log.d(tag, "No such document"); }
            }
            else { Log.d(tag, "get failed with ", task.getException()); }
        });*/



    }

}
