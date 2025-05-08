package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class TopUpActivity extends AppCompatActivity {
    private EditText amountEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_up);

        // Initialize views
        amountEditText = findViewById(R.id.amountEditText);
        Button topUpButton = findViewById(R.id.topUpButton);
        ImageButton backButton = findViewById(R.id.backButton);

        // Back button click listener
        backButton.setOnClickListener(v -> finish());

        // Top up button click listener
        topUpButton.setOnClickListener(v -> handleTopUp());
    }

    private void handleTopUp() {
        String amountStr = amountEditText.getText().toString();
        if (!amountStr.isEmpty()) {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount > 0) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("amount", amount);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                } else {
                    showError("Please enter a valid amount");
                }
            } catch (NumberFormatException e) {
                showError("Please enter a valid number");
            }
        } else {
            showError("Please enter an amount");
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}