package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class CustomDialog extends AppCompatActivity {

    Button button;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_up);
        setContentView(R.layout.activity_send);
        setContentView(R.layout.activity_pay_bills);

        button = findViewById(R.id.show_dialog);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
    }
    private void showDialog() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_receipt);
        dialog.getWindow().getAttributes().windowAnimations=R.style.DialogAnimationReport;
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        ImageButton closeDialog=dialog.findViewById(R.id.closeButton);

        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}