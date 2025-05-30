package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.textfield.TextInputLayout;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

public class SendActivity extends AppCompatActivity implements CustomDialog.ReceiptDialogListener {
    private EditText phoneNumberEditText;
    private EditText amountEditText;
    private EditText nameEditText; // RECIPIENT'S name
    private TextView recentTransactionText;
    private TextView amountDisplayText;
    private TextInputLayout phoneNumberInputLayout;
    private TextInputLayout amountInputLayout;
    private Button sendButton;
    private ImageButton backButton;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    private static final int MIN_PHONE_LENGTH = 10;
    private static final int MAX_PHONE_LENGTH = 11;
    private static final double MIN_AMOUNT = 0.01;
    private static final double MAX_AMOUNT = 10000.0;

    private TextWatcher phoneNumberWatcher;
    private TextWatcher amountWatcher;

    private static final String TRANSACTION_PREFS_NAME = "TransactionPrefs";
    private static final String RECENT_TRANSACTION_KEY = "recentTransaction";

    private static final String USER_PREFS_NAME = "UserPrefs";
    private static final String USER_BALANCE_KEY = "user_balance";
    private static final String LOGGED_IN_USER_NAME_KEY = "userName";

    private double lastProcessedSentAmount;
    private String lastProcessedSentDescription;
    private String lastProcessedSentTransactionNumber;

    private String loggedInSenderName; // SENDER'S name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        initializeViews();
        loadSenderName();
        setupListeners();
        loadRecentTransaction();
        validateInputs();
        initializeBalanceForTesting(1000.00);
    }

    private void loadSenderName() {
        SharedPreferences userPrefs = getSharedPreferences(USER_PREFS_NAME, Context.MODE_PRIVATE);
        this.loggedInSenderName = userPrefs.getString(LOGGED_IN_USER_NAME_KEY, null);

        if (this.loggedInSenderName == null) {
            Log.e("SendActivity", "Logged-in sender's name not found in UserPrefs.");
            this.loggedInSenderName = "Unknown Sender";
            Log.w("SendActivity", "Sender name not found, defaulting to 'Unknown Sender'.");
        } else {
            Log.d("SendActivity", "Sender loaded: " + this.loggedInSenderName);
        }
    }

    private void initializeBalanceForTesting(double initialBalance) {
        SharedPreferences userPrefs = getSharedPreferences(USER_PREFS_NAME, Context.MODE_PRIVATE);
        if (!userPrefs.contains(USER_BALANCE_KEY)) {
            SharedPreferences.Editor editor = userPrefs.edit();
            editor.putFloat(USER_BALANCE_KEY, (float) initialBalance);
            editor.apply();
            Log.d("SendActivity", "Initial balance set for testing: " + currencyFormat.format(initialBalance));
        } else {
            Log.d("SendActivity", "Current balance: " + currencyFormat.format(getCurrentUserBalance()));
        }
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
        amountDisplayText.setText(currencyFormat.format(0));
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        phoneNumberWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { validateInputs(); }
        };
        phoneNumberEditText.addTextChangedListener(phoneNumberWatcher);

        amountWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String amountStr = s.toString();
                if (!amountStr.isEmpty()) {
                    try {
                        double amount = Double.parseDouble(amountStr);
                        amountDisplayText.setText(currencyFormat.format(amount));
                    } catch (NumberFormatException e) {
                        amountDisplayText.setText(currencyFormat.format(0));
                    }
                } else {
                    amountDisplayText.setText(currencyFormat.format(0));
                }
                validateInputs();
            }
        };
        amountEditText.addTextChangedListener(amountWatcher);

        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { validateInputs(); }
        });

        sendButton.setOnClickListener(v -> processPayment());
    }

    private void loadRecentTransaction() {
        SharedPreferences prefs = getSharedPreferences(TRANSACTION_PREFS_NAME, Context.MODE_PRIVATE);
        String recentTransaction = prefs.getString(RECENT_TRANSACTION_KEY, "No recent transaction");
        recentTransactionText.setText("Recent: " + recentTransaction);
    }

    private void saveRecentTransaction(String transactionNumber, String recipientName, String amount) {
        SharedPreferences prefs = getSharedPreferences(TRANSACTION_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(RECENT_TRANSACTION_KEY, amount + " to " + recipientName + " (" + transactionNumber.substring(Math.max(0, transactionNumber.length() - 6)) + ")");
        editor.apply();
        loadRecentTransaction();
    }

    private void validateInputs() {
        boolean isPhoneValid = validatePhoneNumber(phoneNumberEditText.getText().toString());
        boolean isAmountValid = validateAmount(amountEditText.getText().toString());
        boolean isNameValid = !nameEditText.getText().toString().trim().isEmpty();
        sendButton.setEnabled(isPhoneValid && isAmountValid && isNameValid);
        sendButton.setAlpha(sendButton.isEnabled() ? 1.0f : 0.5f);
    }

    private boolean validatePhoneNumber(String phoneNumber) {
        String sanitizedPhone = phoneNumber.replaceAll("[^0-9]", "");
        if (sanitizedPhone.length() >= MIN_PHONE_LENGTH && sanitizedPhone.length() <= MAX_PHONE_LENGTH) {
            phoneNumberInputLayout.setError(null);
            return true;
        } else {
            if (phoneNumber.isEmpty()) {
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
                }
                double currentUserBalance = getCurrentUserBalance();
                if (amount > currentUserBalance) {
                    amountInputLayout.setError("Insufficient balance (" + currencyFormat.format(currentUserBalance) + ")");
                    return false;
                }
                amountInputLayout.setError(null);
                return true;
            } catch (NumberFormatException e) {
                amountInputLayout.setError("Please enter a valid number");
                return false;
            }
        } else {
            amountInputLayout.setError(null);
            return false;
        }
    }

    private double getCurrentUserBalance() {
        SharedPreferences userPrefs = getSharedPreferences(USER_PREFS_NAME, Context.MODE_PRIVATE);
        return userPrefs.getFloat(USER_BALANCE_KEY, 0.0f);
    }

    private void updateUserBalance(double newBalance) {
        SharedPreferences userPrefs = getSharedPreferences(USER_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = userPrefs.edit();
        editor.putFloat(USER_BALANCE_KEY, (float) newBalance);
        editor.apply();
        Log.d("SendActivity", "User balance updated to: " + currencyFormat.format(newBalance));
    }

    private boolean recordTransactionOnServer(String transactionNumber, String senderName, String recipientName, double amount, String recipientPhone) {
        Log.i("SendActivity_ServerSim", "Attempting to record transaction on server: " +
                "ID=" + transactionNumber +
                ", From=" + senderName +
                ", To=" + recipientName + " (" + recipientPhone + ")" +
                ", Amount=" + currencyFormat.format(amount));
        Log.i("SendActivity_ServerSim", "Server Sim: Transaction SUCCESSFUL for " + transactionNumber);
        return true; // Simulate server success
    }

    private void processPayment() {
        validateInputs();
        if (!sendButton.isEnabled()) {
            if (phoneNumberInputLayout.getError() != null || phoneNumberEditText.getText().toString().trim().isEmpty()) {
                showError("Please enter a valid phone number.");
            } else if (nameEditText.getText().toString().trim().isEmpty()){
                showError("Please enter the recipient's name.");
            } else if (amountInputLayout.getError() != null || amountEditText.getText().toString().trim().isEmpty()){
                if (amountInputLayout.getError() != null && amountInputLayout.getError().toString().contains("Insufficient balance")) {
                    showError(amountInputLayout.getError().toString());
                } else {
                    showError("Please enter a valid amount.");
                }
            } else {
                showError("Please correct the errors before proceeding.");
            }
            return;
        }

        if (this.loggedInSenderName == null || this.loggedInSenderName.equals("Unknown Sender") || this.loggedInSenderName.trim().isEmpty()) {
            showError("Cannot process payment: Your sender information is missing. Please re-login or check your profile.");
            return;
        }

        String recipientPhoneNumber = phoneNumberEditText.getText().toString().replaceAll("[^0-9]", "");
        String amountStr = amountEditText.getText().toString();
        String recipientName = nameEditText.getText().toString().trim();
        double amountToSend = Double.parseDouble(amountStr);

        double currentUserBalance = getCurrentUserBalance();
        if (amountToSend > currentUserBalance) {
            showError("Insufficient balance. Your current balance is " + currencyFormat.format(currentUserBalance));
            amountInputLayout.setError("Insufficient balance (" + currencyFormat.format(currentUserBalance) + ")");
            sendButton.setEnabled(false);
            sendButton.setAlpha(0.5f);
            return;
        }

        String formattedPhone = recipientPhoneNumber;
        if (recipientPhoneNumber.length() == 10) {
            formattedPhone = String.format("(%s) %s-%s", recipientPhoneNumber.substring(0, 3), recipientPhoneNumber.substring(3, 6), recipientPhoneNumber.substring(6));
        } else if (recipientPhoneNumber.length() == 11 && (recipientPhoneNumber.startsWith("1") || recipientPhoneNumber.startsWith("0"))) {
            formattedPhone = String.format("+%s (%s) %s-%s", recipientPhoneNumber.charAt(0), recipientPhoneNumber.substring(1, 4), recipientPhoneNumber.substring(4, 7), recipientPhoneNumber.substring(7));
        }

        String transactionNumber = "SEND_" + System.currentTimeMillis();

        boolean transactionSuccessfulOnServer = recordTransactionOnServer(
                transactionNumber,
                this.loggedInSenderName,
                recipientName,
                amountToSend,
                recipientPhoneNumber
        );

        if (transactionSuccessfulOnServer) {
            updateUserBalance(currentUserBalance - amountToSend);
            lastProcessedSentAmount = amountToSend;
            lastProcessedSentDescription = "Sent to " + recipientName + " (" + formattedPhone + ")";
            lastProcessedSentTransactionNumber = transactionNumber;
            saveRecentTransaction(transactionNumber, recipientName, currencyFormat.format(amountToSend));

            ReceiptDetails receiptData = new ReceiptDetails(
                    "Send Money",
                    currencyFormat.format(amountToSend),
                    transactionNumber,
                    new Date().getTime(),
                    true,
                    "Successfully sent money.",
                    this.loggedInSenderName,
                    recipientName
            );
            showReceipt(receiptData);
        } else {
            showError("Transaction failed on server. Please try again later.");
            ReceiptDetails receiptData = new ReceiptDetails(
                    "Send Money",
                    currencyFormat.format(amountToSend),
                    transactionNumber,
                    new Date().getTime(),
                    false,
                    "Transaction could not be processed by the server.",
                    this.loggedInSenderName,
                    recipientName
            );
            // showReceipt(receiptData); // Optional: show failure receipt
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showReceipt(ReceiptDetails details) {
        CustomDialog dialogFragment = CustomDialog.newInstance(details);
        dialogFragment.setReceiptDialogListener(this);
        dialogFragment.show(getSupportFragmentManager(), CustomDialog.TAG);
    }

    @Override
    public void onReceiptDialogClose(DialogFragment dialog) {
        Intent resultIntent = new Intent();
        if (lastProcessedSentTransactionNumber != null && !lastProcessedSentTransactionNumber.isEmpty()){
            resultIntent.putExtra("amount", lastProcessedSentAmount);
            resultIntent.putExtra("description", lastProcessedSentDescription);
            resultIntent.putExtra("transactionNumber", lastProcessedSentTransactionNumber);
            setResult(RESULT_OK, resultIntent);
        } else {
            setResult(RESULT_CANCELED, resultIntent);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (phoneNumberWatcher != null) {
            phoneNumberEditText.removeTextChangedListener(phoneNumberWatcher);
        }
        if (amountWatcher != null) {
            amountEditText.removeTextChangedListener(amountWatcher);
        }
    }
}