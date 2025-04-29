package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.NumberFormat;
import java.util.Locale;

public class PayBillsActivity extends AppCompatActivity {
    private Spinner billTypeSpinner;
    private EditText amountEditText;
    private EditText accountNumberEditText;
    private TextView amountDisplayText;
    private Button payButton;
    private double currentAmount = 0.0;
    private static final double MIN_AMOUNT = 1.0;
    private static final double MAX_AMOUNT = 10000.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_bills);

        initializeViews();
        setupSpinner();
        setupListeners();
    }

    private void initializeViews() {
        billTypeSpinner = findViewById(R.id.billTypeSpinner);
        amountEditText = findViewById(R.id.amountEditText);
        accountNumberEditText = findViewById(R.id.accountNumberEditText);
        amountDisplayText = findViewById(R.id.amountDisplayText);
        payButton = findViewById(R.id.payButton);
        ImageButton backButton = findViewById(R.id.backButton);

        // Set initial amount display
        updateAmountDisplay(0.0);

        // Setup back button
        backButton.setOnClickListener(v -> finish());
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.bill_types, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        billTypeSpinner.setAdapter(adapter);

        billTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateAccountNumberHint(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupListeners() {
        amountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (!s.toString().isEmpty()) {
                        currentAmount = Double.parseDouble(s.toString());
                        updateAmountDisplay(currentAmount);
                    } else {
                        currentAmount = 0.0;
                        updateAmountDisplay(0.0);
                    }
                } catch (NumberFormatException e) {
                    currentAmount = 0.0;
                    updateAmountDisplay(0.0);
                }
                validateInputs();
            }
        });

        accountNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateInputs();
            }
        });

        payButton.setOnClickListener(v -> processPayment());
    }

    private void updateAccountNumberHint(String billType) {
        String hint;
        switch (billType) {
            case "Electricity":
                hint = "Enter Meter Number";
                accountNumberEditText.setHint(hint);
                break;
            case "Water":
                hint = "Enter Customer ID";
                accountNumberEditText.setHint(hint);
                break;
            case "Internet":
                hint = "Enter Account Number";
                accountNumberEditText.setHint(hint);
                break;
            case "Phone":
                hint = "Enter Phone Number";
                accountNumberEditText.setHint(hint);
                break;
            default:
                hint = "Enter Account Number";
                accountNumberEditText.setHint(hint);
                break;
        }
    }

    private void updateAmountDisplay(double amount) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        amountDisplayText.setText(currencyFormat.format(amount));
    }

    private void validateInputs() {
        boolean isValid = !accountNumberEditText.getText().toString().trim().isEmpty() &&
                currentAmount >= MIN_AMOUNT &&
                currentAmount <= MAX_AMOUNT;
        payButton.setEnabled(isValid);
        payButton.setAlpha(isValid ? 1.0f : 0.5f);
    }

    private void processPayment() {
        String billType = billTypeSpinner.getSelectedItem().toString();
        String accountNumber = accountNumberEditText.getText().toString().trim();

        if (accountNumber.isEmpty()) {
            showError("Please enter account number");
            return;
        }

        if (currentAmount < MIN_AMOUNT) {
            showError("Minimum amount is $" + MIN_AMOUNT);
            return;
        }

        if (currentAmount > MAX_AMOUNT) {
            showError("Maximum amount is $" + MAX_AMOUNT);
            return;
        }

        // Get current balance from intent
        double currentBalance = getIntent().getDoubleExtra("currentBalance", 0.0);
        if (currentAmount > currentBalance) {
            showError("Insufficient balance");
            return;
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("amount", currentAmount);
        resultIntent.putExtra("description", billType + " - " + accountNumber);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}