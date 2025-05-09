package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
    private EditText nameEditText;
    private TextView recentTransactionText;
    private TextView amountDisplayText;
    private TextInputLayout phoneNumberInputLayout;
    private TextInputLayout amountInputLayout;
    private Button sendButton;
    private ImageButton backButton;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    // Constants
    private static final int MIN_PHONE_LENGTH = 10;
    private static final int MAX_PHONE_LENGTH = 11;
    private static final double MIN_AMOUNT = 0.01;
    private static final double MAX_AMOUNT = 10000.0;

    private TextWatcher phoneNumberWatcher;
    private TextWatcher amountWatcher;

    private static final String PREFS_NAME = "TransactionPrefs";
    private static final String RECENT_TRANSACTION_KEY = "recentTransaction";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        initializeViews();
        setupListeners();
        loadRecentTransaction();
    }

    private void initializeViews() {
        phoneNumberInputLayout = findViewById(R.id.phoneNumberInputLayout);
        amountInputLayout = findViewById(R.id.amountInputLayout);
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        amountEditText = findViewById(R.id.amountEditText);
        nameEditText = findViewById(R.id.nameEditText); // New input for name
        recentTransactionText = findViewById(R.id.recentTransactionText); // TextView for recent transaction
        amountDisplayText = findViewById(R.id.amountDisplayText);
        sendButton = findViewById(R.id.sendButton);
        backButton = findViewById(R.id.backButton);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        // Phone number validation
        phoneNumberWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validatePhoneNumber(s.toString());
            }
        };
        phoneNumberEditText.addTextChangedListener(phoneNumberWatcher);

        // Amount input formatting
        amountWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateAmount(s.toString());
            }
        };
        amountEditText.addTextChangedListener(amountWatcher);

        sendButton.setOnClickListener(v -> processPayment());
    }

    private void loadRecentTransaction() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String recentTransaction = prefs.getString(RECENT_TRANSACTION_KEY, "No recent transaction");
        recentTransactionText.setText("Recent Transaction: " + recentTransaction);
    }

    private void saveRecentTransaction(String transactionNumber) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(RECENT_TRANSACTION_KEY, transactionNumber);
        editor.apply();
    }

    private void validatePhoneNumber(String phoneNumber) {
        String sanitizedPhone = phoneNumber.replaceAll("[^0-9]", "");
        if (sanitizedPhone.length() < MIN_PHONE_LENGTH || sanitizedPhone.length() > MAX_PHONE_LENGTH) {
            phoneNumberInputLayout.setError("Phone number must be 10-11 digits");
        } else {
            phoneNumberInputLayout.setError(null);
        }
    }

    private void validateAmount(String amountStr) {
        if (!amountStr.isEmpty()) {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount < MIN_AMOUNT) {
                    amountInputLayout.setError("Amount must be greater than $0.01");
                } else if (amount > MAX_AMOUNT) {
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

    private void processPayment() {
        String phoneNumber = phoneNumberEditText.getText().toString().replaceAll("[^0-9]", "");
        String amountStr = amountEditText.getText().toString();
        String name = nameEditText.getText().toString();

        if (phoneNumberInputLayout.getError() != null || phoneNumber.isEmpty()) {
            showError("Please enter a valid phone number");
            return;
        }

        if (amountInputLayout.getError() != null || amountStr.isEmpty()) {
            showError("Please enter a valid amount");
            return;
        }

        if (name.isEmpty()) {
            showError("Please enter a name");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            // Format phone number for display
            String formattedPhone = String.format("(%s) %s-%s",
                    phoneNumber.substring(0, 3),
                    phoneNumber.substring(3, 6),
                    phoneNumber.substring(6));

            String transactionNumber = "TXN" + System.currentTimeMillis();
            saveRecentTransaction(transactionNumber);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("amount", amount);
            resultIntent.putExtra("description", "Send to " + formattedPhone + " (" + name + ")");
            resultIntent.putExtra("transactionNumber", transactionNumber);
            setResult(RESULT_OK, resultIntent);
            finish();

        } catch (NumberFormatException e) {
            showError("Please enter a valid number");
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove TextWatchers to prevent memory leaks
        if (phoneNumberWatcher != null) {
            phoneNumberEditText.removeTextChangedListener(phoneNumberWatcher);
        }
        if (amountWatcher != null) {
            amountEditText.removeTextChangedListener(amountWatcher);
        }
    }
}