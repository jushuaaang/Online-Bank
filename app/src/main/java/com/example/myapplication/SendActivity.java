package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import java.text.NumberFormat;
import java.util.Locale;

public class SendActivity extends AppCompatActivity {
    private EditText phoneNumberEditText;
    private EditText amountEditText;
    private TextView amountDisplayText;
    private TextInputLayout phoneNumberInputLayout;
    private TextInputLayout amountInputLayout;
    private Button sendButton;
    private ImageButton backButton;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        phoneNumberInputLayout = findViewById(R.id.phoneNumberInputLayout);
        amountInputLayout = findViewById(R.id.amountInputLayout);
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        amountEditText = findViewById(R.id.amountEditText);
        amountDisplayText = findViewById(R.id.amountDisplayText);
        sendButton = findViewById(R.id.sendButton);
        backButton = findViewById(R.id.backButton);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        // Phone number validation
        phoneNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String phone = s.toString().replaceAll("[^0-9]", "");
                if (phone.length() < 10) {
                    phoneNumberInputLayout.setError("Please enter a valid phone number");
                } else {
                    phoneNumberInputLayout.setError(null);
                }
            }
        });

        // Amount input formatting
        amountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String amountStr = s.toString();
                if (!amountStr.isEmpty()) {
                    try {
                        double amount = Double.parseDouble(amountStr);
                        if (amount <= 0) {
                            amountInputLayout.setError("Amount must be greater than 0");
                        } else if (amount > 10000) {
                            amountInputLayout.setError("Maximum amount is $10,000");
                        } else {
                            amountInputLayout.setError(null);
                            amountDisplayText.setText(currencyFormat.format(amount));
                        }
                    } catch (NumberFormatException e) {
                        amountInputLayout.setError("Please enter a valid number");
                    }
                } else {
                    amountInputLayout.setError(null);
                    amountDisplayText.setText(currencyFormat.format(0));
                }
            }
        });

        sendButton.setOnClickListener(v -> processPayment());
    }

    private void processPayment() {
        String phoneNumber = phoneNumberEditText.getText().toString().replaceAll("[^0-9]", "");
        String amountStr = amountEditText.getText().toString();

        if (phoneNumber.length() < 10) {
            showError("Please enter a valid phone number");
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

            if (amount > 10000) {
                showError("Maximum amount is $10,000");
                return;
            }

            // Format phone number for display
            String formattedPhone = String.format("(%s) %s-%s",
                    phoneNumber.substring(0, 3),
                    phoneNumber.substring(3, 6),
                    phoneNumber.substring(6));

            Intent resultIntent = new Intent();
            resultIntent.putExtra("amount", amount);
            resultIntent.putExtra("description", "Send to " + formattedPhone);
            setResult(RESULT_OK, resultIntent);
            finish();

        } catch (NumberFormatException e) {
            showError("Please enter a valid number");
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}