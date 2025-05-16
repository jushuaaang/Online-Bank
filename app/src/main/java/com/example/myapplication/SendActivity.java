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
import androidx.fragment.app.DialogFragment; // Import DialogFragment
import com.google.android.material.textfield.TextInputLayout;
import java.text.NumberFormat;
import java.util.Date; // Import Date
import java.util.Locale;

// Add implements ReceiptDialogFragment.ReceiptDialogListener
public class SendActivity extends AppCompatActivity implements CustomDialog.ReceiptDialogListener {
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

    private static final int MIN_PHONE_LENGTH = 10;
    private static final int MAX_PHONE_LENGTH = 11; // Adjusted to typical US phone length
    private static final double MIN_AMOUNT = 0.01;
    private static final double MAX_AMOUNT = 10000.0;

    private TextWatcher phoneNumberWatcher;
    private TextWatcher amountWatcher;

    private static final String PREFS_NAME = "TransactionPrefs";
    private static final String RECENT_TRANSACTION_KEY = "recentTransaction";

    // To store data for onReceiptDialogClose
    private double lastProcessedSentAmount;
    private String lastProcessedSentDescription;
    private String lastProcessedSentTransactionNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        initializeViews();
        setupListeners();
        loadRecentTransaction();
        validateInputs(); // Initial validation for button state
    }

    private void initializeViews() {
        phoneNumberInputLayout = findViewById(R.id.phoneNumberInputLayout);
        amountInputLayout = findViewById(R.id.amountInputLayout);
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        amountEditText = findViewById(R.id.amountEditText);
        nameEditText = findViewById(R.id.nameEditText);
        recentTransactionText = findViewById(R.id.recentTransactionText);
        amountDisplayText = findViewById(R.id.amountDisplayText);
        sendButton = findViewById(R.id.sendButton);
        backButton = findViewById(R.id.backButton);
        amountDisplayText.setText(currencyFormat.format(0)); // Initialize
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        phoneNumberWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                validateInputs(); // Consolidate validation
            }
        };
        phoneNumberEditText.addTextChangedListener(phoneNumberWatcher);

        amountWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String amountStr = s.toString();
                if (!amountStr.isEmpty()) {
                    try {
                        double amount = Double.parseDouble(amountStr);
                        amountDisplayText.setText(currencyFormat.format(amount));
                    } catch (NumberFormatException e) {
                        amountDisplayText.setText(currencyFormat.format(0)); // Reset on error
                    }
                } else {
                    amountDisplayText.setText(currencyFormat.format(0));
                }
                validateInputs(); // Consolidate validation
            }
        };
        amountEditText.addTextChangedListener(amountWatcher);

        nameEditText.addTextChangedListener(new TextWatcher() { // Add watcher for name
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                validateInputs();
            }
        });


        sendButton.setOnClickListener(v -> processPayment());
    }

    private void loadRecentTransaction() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String recentTransaction = prefs.getString(RECENT_TRANSACTION_KEY, "No recent transaction");
        recentTransactionText.setText("Recent: " + recentTransaction);
    }

    private void saveRecentTransaction(String transactionNumber, String recipientName, String amount) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        // Save more descriptive recent transaction
        editor.putString(RECENT_TRANSACTION_KEY, amount + " to " + recipientName + " (" + transactionNumber.substring(Math.max(0, transactionNumber.length() - 6)) + ")");
        editor.apply();
        loadRecentTransaction(); // Refresh display
    }

    // Combined validation method
    private void validateInputs() {
        boolean isPhoneValid = validatePhoneNumber(phoneNumberEditText.getText().toString());
        boolean isAmountValid = validateAmount(amountEditText.getText().toString());
        boolean isNameValid = !nameEditText.getText().toString().trim().isEmpty();

        if (nameEditText.getText().toString().trim().isEmpty()){
            // Optionally set an error on nameEditText's TextInputLayout if you add one
            // For now, just using it for button state
        }

        sendButton.setEnabled(isPhoneValid && isAmountValid && isNameValid);
        sendButton.setAlpha(sendButton.isEnabled() ? 1.0f : 0.5f);
    }


    private boolean validatePhoneNumber(String phoneNumber) {
        String sanitizedPhone = phoneNumber.replaceAll("[^0-9]", "");
        if (sanitizedPhone.length() >= MIN_PHONE_LENGTH && sanitizedPhone.length() <= MAX_PHONE_LENGTH) {
            phoneNumberInputLayout.setError(null);
            return true;
        } else {
            if (phoneNumber.isEmpty()){ // Don't show error if empty yet, only if typed and invalid
                phoneNumberInputLayout.setError(null);
            } else {
                phoneNumberInputLayout.setError("Phone number must be " + MIN_PHONE_LENGTH + "-" + MAX_PHONE_LENGTH + " digits");
            }
            return false;
        }
    }

    private boolean validateAmount(String amountStr) {
        if (!amountStr.isEmpty()) {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount < MIN_AMOUNT) {
                    amountInputLayout.setError("Amount must be at least " + currencyFormat.format(MIN_AMOUNT));
                    return false;
                } else if (amount > MAX_AMOUNT) {
                    amountInputLayout.setError("Maximum amount is " + currencyFormat.format(MAX_AMOUNT));
                    return false;
                } else {
                    amountInputLayout.setError(null);
                    // amountDisplayText is updated in text watcher
                    return true;
                }
            } catch (NumberFormatException e) {
                amountInputLayout.setError("Please enter a valid number");
                return false;
            }
        } else {
            amountInputLayout.setError(null); // Clear error if empty, button state will handle requirement
            return false; // Or true if amount can be optional until send, depends on UX
        }
    }

    private void processPayment() {
        validateInputs(); // Final validation check
        if (!sendButton.isEnabled()) {
            // Find which field is causing the issue and show a more specific toast or focus
            if (phoneNumberInputLayout.getError() != null || phoneNumberEditText.getText().toString().trim().isEmpty()) {
                showError("Please enter a valid phone number.");
            } else if (nameEditText.getText().toString().trim().isEmpty()){
                showError("Please enter the recipient's name.");
            } else if (amountInputLayout.getError() != null || amountEditText.getText().toString().trim().isEmpty()){
                showError("Please enter a valid amount.");
            } else {
                showError("Please correct the errors before proceeding.");
            }
            return;
        }

        String phoneNumber = phoneNumberEditText.getText().toString().replaceAll("[^0-9]", "");
        String amountStr = amountEditText.getText().toString();
        String name = nameEditText.getText().toString().trim();
        double amount = Double.parseDouble(amountStr); // Already validated

        String formattedPhone = phoneNumber; // Keep raw for processing, format for display if needed later
        if (phoneNumber.length() == 10) { // Basic US formatting example
            formattedPhone = String.format("(%s) %s-%s", phoneNumber.substring(0, 3), phoneNumber.substring(3, 6), phoneNumber.substring(6));
        } else if (phoneNumber.length() == 11 && (phoneNumber.startsWith("1") || phoneNumber.startsWith("0"))) { // e.g. +1 or 09...
            formattedPhone = String.format("+%s (%s) %s-%s", phoneNumber.charAt(0), phoneNumber.substring(1, 4), phoneNumber.substring(4, 7), phoneNumber.substring(7));
        }


        String transactionNumber = "SEND_" + System.currentTimeMillis(); // Changed prefix

        // Store for onReceiptDialogClose
        lastProcessedSentAmount = amount;
        lastProcessedSentDescription = "Sent to " + name + " (" + formattedPhone + ")";
        lastProcessedSentTransactionNumber = transactionNumber;


        saveRecentTransaction(transactionNumber, name, currencyFormat.format(amount));

        ReceiptDetails receiptData = new ReceiptDetails(
                "Send Money", // Transaction Type for the receipt title
                currencyFormat.format(amount),
                transactionNumber,
                new Date().getTime(),
                true, // Assuming success
                "Successfully sent money.", // General success message
                name, // Recipient Name
                null  // No biller name for send money
        );
        showReceipt(receiptData);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // --- ADDED FOR RECEIPT DIALOG ---
    private void showReceipt(ReceiptDetails details) {
        CustomDialog dialogFragment = CustomDialog.newInstance(details);
        dialogFragment.setReceiptDialogListener(this);
        dialogFragment.show(getSupportFragmentManager(), CustomDialog.TAG);
    }

    @Override
    public void onReceiptDialogClose(DialogFragment dialog) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("amount", lastProcessedSentAmount);
        resultIntent.putExtra("description", lastProcessedSentDescription);
        resultIntent.putExtra("transactionNumber", lastProcessedSentTransactionNumber);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
    // --- END OF ADDED CODE ---

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (phoneNumberWatcher != null) {
            phoneNumberEditText.removeTextChangedListener(phoneNumberWatcher);
        }
        if (amountWatcher != null) {
            amountEditText.removeTextChangedListener(amountWatcher);
        }
        // Also remove listener for nameEditText if you add one with a member variable
    }
}