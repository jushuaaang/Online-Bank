package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import java.text.NumberFormat;
import java.util.Locale;

public class PayBillsActivity extends AppCompatActivity {
    private Spinner billTypeSpinner;
    private EditText amountEditText;
    private EditText accountNumberEditText;
    private TextView amountDisplayText;
    private Button payButton;
    private TextInputLayout accountNumberInputLayout;
    private TextInputLayout amountInputLayout;
    private double currentAmount = 0.0;
    private static final double MIN_AMOUNT = 1.0;
    private static final double MAX_AMOUNT = 10000.0;
    private static final double SPOTIFY_AMOUNT = 9.99;
    private static final double NETFLIX_AMOUNT = 15.49;

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
        accountNumberInputLayout = findViewById(R.id.accountNumberInputLayout);
        amountInputLayout = findViewById(R.id.amountInputLayout);
        ImageButton backButton = findViewById(R.id.backButton);

        updateAmountDisplay(0.0);
        backButton.setOnClickListener(v -> finish());
    }

    private void setupSpinner() {
        BillTypeAdapter adapter = new BillTypeAdapter(this, getResources().getStringArray(R.array.bill_types));
        billTypeSpinner.setAdapter(adapter);

        billTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedBill = (String) parent.getItemAtPosition(position);
                updateAccountNumberHint(selectedBill);
                updateAmountForSubscription(selectedBill);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupListeners() {
        amountEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String selectedBill = billTypeSpinner.getSelectedItem().toString();
                if (selectedBill.equals("Spotify") || selectedBill.equals("Netflix")) return;

                try {
                    if (!s.toString().isEmpty()) {
                        currentAmount = Double.parseDouble(s.toString());
                        if (currentAmount < MIN_AMOUNT) {
                            amountInputLayout.setError("Minimum amount is $" + MIN_AMOUNT);
                        } else if (currentAmount > MAX_AMOUNT) {
                            amountInputLayout.setError("Maximum amount is $" + MAX_AMOUNT);
                        } else {
                            amountInputLayout.setError(null);
                        }
                    } else {
                        currentAmount = 0.0;
                        amountInputLayout.setError(null);
                    }
                    updateAmountDisplay(currentAmount);
                } catch (NumberFormatException e) {
                    amountInputLayout.setError("Please enter a valid number");
                    currentAmount = 0.0;
                    updateAmountDisplay(0.0);
                }
                //validateInputs();
            }
        });

        accountNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().isEmpty()) {
                    accountNumberInputLayout.setError("Please enter account number");
                } else {
                    accountNumberInputLayout.setError(null);
                }
               // validateInputs();
            }
        });

        payButton.setOnClickListener(v -> processPayment());
    }

    private void updateAccountNumberHint(String billType) {
        String hint;
        switch (billType) {
            case "Electricity":
                hint = "Enter Meter Number";
                accountNumberEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
            case "Water":
                hint = "Enter Customer ID";
                accountNumberEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
            case "Gas":
                hint = "Enter Account Number";
                accountNumberEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
            case "Internet":
                hint = "Enter Internet Account ID";
                accountNumberEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
            case "Spotify":
                hint = "Enter Spotify Username";
                accountNumberEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                break;
            case "Netflix":
                hint = "Enter Netflix Email";
                accountNumberEditText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | InputType.TYPE_CLASS_TEXT);
                break;
            case "Phone":
                hint = "Enter Mobile Number";
                accountNumberEditText.setInputType(InputType.TYPE_CLASS_PHONE);
                break;
            default:
                hint = "Enter Account Number";
                accountNumberEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
        }
        accountNumberInputLayout.setHint(hint);
    }

    private void updateAmountForSubscription(String billType) {
        switch (billType) {
            case "Spotify":
                amountEditText.setText(String.valueOf(SPOTIFY_AMOUNT));
                amountEditText.setEnabled(false);
                currentAmount = SPOTIFY_AMOUNT;
                amountInputLayout.setHint("Subscription Amount");
                break;
            case "Netflix":
                amountEditText.setText(String.valueOf(NETFLIX_AMOUNT));
                amountEditText.setEnabled(false);
                currentAmount = NETFLIX_AMOUNT;
                amountInputLayout.setHint("Subscription Amount");
                break;
            case "Internet":
                amountEditText.setText("");
                amountEditText.setEnabled(true);
                amountInputLayout.setHint("Enter Monthly Bill Amount");
                amountEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                currentAmount = 0.0;
                break;
            case "Phone":
                amountEditText.setText("");
                amountEditText.setEnabled(true);
                amountInputLayout.setHint("Enter Load Amount");
                amountEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                currentAmount = 0.0;
                break;
            default:
                amountEditText.setText("");
                amountEditText.setEnabled(true);
                amountInputLayout.setHint("Amount");
                amountEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                currentAmount = 0.0;
                break;
        }
        updateAmountDisplay(currentAmount);
        //validateInputs();
    }

    private void validateInputs() {
        String accountNumber = accountNumberEditText.getText().toString().trim();
        String selectedBill = billTypeSpinner.getSelectedItem().toString();

        boolean isValidAccount = true;
        if (accountNumber.isEmpty()) {
            accountNumberInputLayout.setError("This field is required");
            isValidAccount = false;
        } else {
            switch (selectedBill) {
                case "Netflix":
                    isValidAccount = android.util.Patterns.EMAIL_ADDRESS.matcher(accountNumber).matches();
                    if (!isValidAccount) {
                        accountNumberInputLayout.setError("Please enter a valid email");
                    } else {
                        accountNumberInputLayout.setError(null);
                    }
                    break;
                case "Electricity":
                    isValidAccount = accountNumber.matches("\\d+");
                    if (!isValidAccount) {
                        accountNumberInputLayout.setError("Please enter a valid meter number");
                    } else {
                        accountNumberInputLayout.setError(null);
                    }
                    break;
                case "Phone":
                    isValidAccount = accountNumber.matches("^\\d{10,11}$");
                    if (!isValidAccount) {
                        accountNumberInputLayout.setError("Enter a valid 10-11 digit number");
                    } else {
                        accountNumberInputLayout.setError(null);
                    }
                    break;
                default:
                    accountNumberInputLayout.setError(null);
                    break;
            }
        }

        boolean isValidAmount = currentAmount >= MIN_AMOUNT && currentAmount <= MAX_AMOUNT;
        if (!isValidAmount && amountEditText.isEnabled()) {
            if (currentAmount < MIN_AMOUNT) {
                amountInputLayout.setError("Minimum amount is $" + MIN_AMOUNT);
            } else if (currentAmount > MAX_AMOUNT) {
                amountInputLayout.setError("Maximum amount is $" + MAX_AMOUNT);
            }
        } else {
            amountInputLayout.setError(null);
        }

        boolean isValid = isValidAccount && isValidAmount;
        payButton.setEnabled(isValid);
        payButton.setAlpha(isValid ? 1.0f : 0.5f);
    }

    private void updateAmountDisplay(double amount) {
        String formattedAmount = NumberFormat.getCurrencyInstance(Locale.US).format(amount);
        amountDisplayText.setText("Amount: " + formattedAmount);
    }

    private void processPayment() {
        String accountNumber = accountNumberEditText.getText().toString();
        String billType = billTypeSpinner.getSelectedItem().toString();
        double amount = currentAmount;

        if (amount < MIN_AMOUNT || amount > MAX_AMOUNT) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }
        validateInputs();
        if (!payButton.isEnabled()) {
            Toast.makeText(this, "Please correct the errors before proceeding", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent();
        intent.putExtra("amount", amount);
        intent.putExtra("billType", billType);
        intent.putExtra("accountNumber", accountNumber);
        intent.putExtra("description","Payment for " + billType + " bill");
        setResult(RESULT_OK, intent);
        finish();
    }
}
