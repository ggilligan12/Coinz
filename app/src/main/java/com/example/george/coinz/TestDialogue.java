package com.example.george.coinz;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class TestDialogue extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.testing_msg)
                .setPositiveButton(R.string.ignore1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {}
                })
                .setNegativeButton(R.string.ignore2, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {}
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}

// Insert into the activity we want to interrupt for testing purposes:
//new TestDialogue().show(getSupportFragmentManager(),"tag"); //Testing