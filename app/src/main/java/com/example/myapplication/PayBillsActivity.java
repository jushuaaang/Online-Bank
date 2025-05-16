package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
// import android.util.Log; // Not used directly in this snippet after modification
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment; // Import DialogFragment
import com.google.android.material.textfield.TextInputLayout;
import java.text.NumberFormat;
import java.util.Date; // Import Date
import java.util.Locale;

// Add implements ReceiptDialogFragment.ReceiptDialogListener
public class PayBillsActivity extends AppCompatActivity implements CustomDialog.ReceiptDialogListener {
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

    // To store data for onReceiptDialogClose if needed by setResult
    private String lastProcessedBillType;
    private String lastProcessedAccountNumber;
    private double lastProcessedAmount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_bills);

        initializeViews();
        setupSpinner();
        setupListeners();
        // You might want to call validateInputs() here if it also handles initial button state
        validateInputs(); // Added to ensure button state is correct on start
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
                validateInputs(); // Call validateInputs after changes
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
                // For Spotify/Netflix, amount is fixed and handled by updateAmountForSubscription
                if (selectedBill.equals("Spotify") || selectedBill.equals("Netflix")) {
                    // currentAmount is already set by updateAmountForSubscription
                    // No further action needed here for amount parsing for these types
                } else {
                    try {
                        if (!s.toString().isEmpty()) {
                            currentAmount = Double.parseDouble(s.toString());
                        } else {
                            currentAmount = 0.0;
                        }
                    } catch (NumberFormatException e) {
                        currentAmount = 0.0; // Reset on error
                    }
                }
                updateAmountDisplay(currentAmount); // Always update display
                validateInputs(); // Call validateInputs after changes
            }
        });

        accountNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                validateInputs(); // Call validateInputs after changes
            }
        });

        payButton.setOnClickListener(v -> processPayment());
    }

    private void updateAccountNumberHint(String billType) {
        String hint;
        // Reset error for account number when bill type changes
        accountNumberInputLayout.setError(null);
        switch (billType) {
            case "Electricity":
                hint = "Enter Meter Number";
                accountNumberEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
            case "Water":
                hint = "Enter Customer ID";
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
            case "Gas":
            default:
                hint = "Enter Account Number";
                accountNumberEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
        }
        accountNumberInputLayout.setHint(hint);
        accountNumberEditText.setText(""); // Clear account number field when type changes
    }

    private void updateAmountForSubscription(String billType) {
        // Reset error for amount when bill type changes
        amountInputLayout.setError(null);
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
            // case "Internet": // Your existing logic for Internet, Phone, default
            // case "Phone":
            default:
                amountEditText.setText(""); // Clear for others
                amountEditText.setEnabled(true);
                if (billType.equals("Internet")) {
                    amountInputLayout.setHint("Enter Monthly Bill Amount");
                } else if (billType.equals("Phone")) {
                    amountInputLayout.setHint("Enter Load Amount");
                } else {
                    amountInputLayout.setHint("Amount");
                }
                amountEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                currentAmount = 0.0; // Reset currentAmount if field is cleared or type changes to non-subscription
                break;
        }
        updateAmountDisplay(currentAmount);
        // validateInputs(); // Called by onItemSelected listener in setupSpinner
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
                    accountNumberInputLayout.setError(isValidAccount ? null : "Please enter a valid email");
                    break;
                case "Electricity":
                    isValidAccount = accountNumber.matches("\\d+"); // Check if all digits
                    accountNumberInputLayout.setError(isValidAccount ? null : "Please enter a valid meter number");
                    break;
                case "Phone":
                    isValidAccount = accountNumber.matches("^\\d{10,11}$");
                    accountNumberInputLayout.setError(isValidAccount ? null : "Enter a valid 10-11 digit number");
                    break;
                default:
                    accountNumberInputLayout.setError(null); // Clear error for other types if not empty
                    break;
            }
        }

        boolean isValidAmount = true;
        if (amountEditText.isEnabled()) { // Only validate amount if EditText is enabled
            if (amountEditText.getText().toString().isEmpty() && currentAmount == 0.0) {
                // Show error only if the field is empty AND currentAmount is also 0 (e.g. after programmatic clear)
                amountInputLayout.setError("Amount is required");
                isValidAmount = false;
            } else if (currentAmount < MIN_AMOUNT) {
                amountInputLayout.setError("Minimum amount is " + NumberFormat.getCurrencyInstance(Locale.US).format(MIN_AMOUNT));
                isValidAmount = false;
            } else if (currentAmount > MAX_AMOUNT) {
                amountInputLayout.setError("Maximum amount is " + NumberFormat.getCurrencyInstance(Locale.US).format(MAX_AMOUNT));
                isValidAmount = false;
            } else {
                amountInputLayout.setError(null);
            }
        } else { // If amountEditText is disabled (Spotify/Netflix), amount is considered valid.
            amountInputLayout.setError(null);
            isValidAmount = true;
        }


        boolean enableButton = isValidAccount && isValidAmount;
        payButton.setEnabled(enableButton);
        payButton.setAlpha(enableButton ? 1.0f : 0.5f);
    }

    private void updateAmountDisplay(double amount) {
        String formattedAmount = NumberFormat.getCurrencyInstance(Locale.US).format(amount);
        amountDisplayText.setText(formattedAmount); // Set only the formatted amount
    }

    private void processPayment() {
        // Perform a final validation before proceeding
        validateInputs();
        if (!payButton.isEnabled()) {
            Toast.makeText(this, "Please correct the errors before proceeding", Toast.LENGTH_SHORT).show();
            return;
        }

        String accountNumber = accountNumberEditText.getText().toString().trim();
        String billType = billTypeSpinner.getSelectedItem().toString();
        // currentAmount is already up-to-date

        // Store for onReceiptDialogClose
        lastProcessedBillType = billType;
        lastProcessedAccountNumber = accountNumber;
        lastProcessedAmount = currentAmount;

        String transactionRef = "BILLPAY_" + System.currentTimeMillis();
        ReceiptDetails receiptData = new ReceiptDetails(
                "Bill Payment", // General Transaction Type for the receipt title
                NumberFormat.getCurrencyInstance(Locale.US).format(currentAmount),
                transactionRef,
                new Date().getTime(),
                true, // Assuming success for now
                "Payment for " + billType + " to " + accountNumber,
                null, // No recipient name for bill pay in this context
                billType // Biller name can be the billType itself or more specific if available
        );
        showReceipt(receiptData);
    }

    // --- ADDED FOR RECEIPT DIALOG ---
    private void showReceipt(ReceiptDetails details) {
        CustomDialog dialogFragment = CustomDialog.newInstance(details);
        dialogFragment.setReceiptDialogListener(this);
        dialogFragment.show(getSupportFragmentManager(), CustomDialog.TAG);
    }

    @Override
    public void onReceiptDialogClose(DialogFragment dialog) {
        // This is where you now finish the activity and set the result
        Intent intent = new Intent();
        intent.putExtra("amount", lastProcessedAmount);
        intent.putExtra("billType", lastProcessedBillType);
        intent.putExtra("accountNumber", lastProcessedAccountNumber);
        intent.putExtra("description", "Payment for " + lastProcessedBillType + " bill");
        setResult(RESULT_OK, intent);
        finish();
    }
    // --- END OF ADDED CODE ---
}