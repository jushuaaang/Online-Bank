package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SendActivity extends AppCompatActivity {
    private EditText recipientEditText;
    private EditText amountEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        recipientEditText = findViewById(R.id.recipientEditText);
        amountEditText = findViewById(R.id.amountEditText);
        Button sendButton = findViewById(R.id.sendButton);
        ImageButton backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());

        sendButton.setOnClickListener(v -> {
            String recipient = recipientEditText.getText().toString();
            String amountStr = amountEditText.getText().toString();

            if (recipient.isEmpty()) {
                showError("Please enter recipient name");
                return;
            }

            if (amountStr.isEmpty()) {
                showError("Please enter amount");
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    showError("Please enter a valid amount");
                    return;
                }

                Intent resultIntent = new Intent();
                resultIntent.putExtra("amount", amount);
                resultIntent.putExtra("description", "Send to " + recipient);
                setResult(RESULT_OK, resultIntent);
                finish();

            } catch (NumberFormatException e) {
                showError("Please enter a valid number");
            }
        });
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}