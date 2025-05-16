package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment; // Import DialogFragment

import java.text.NumberFormat; // For currency formatting
import java.util.Date;       // For timestamp
import java.util.Locale;     // For currency formatting

// Assuming ReceiptDialogFragment and ReceiptDetails are in the same package
// or import them: import com.example.myapplication.ReceiptDialogFragment;
// import com.example.myapplication.ReceiptDetails;

public class TopUpActivity extends AppCompatActivity implements CustomDialog.ReceiptDialogListener {
    private EditText amountEditText;
    private Button topUpButton;
    private double currentTopUpAmount; // To store the amount for the receipt

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_up);

        amountEditText = findViewById(R.id.amountEditText);
        topUpButton = findViewById(R.id.topUpButton);
        ImageButton backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());

        topUpButton.setOnClickListener(v -> {
            String amountStr = amountEditText.getText().toString();
            if (!amountStr.isEmpty()) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    if (amount > 0) {
                        currentTopUpAmount = amount; // Store for receipt

                        // --- MODIFICATION: Show receipt dialog ---
                        String accountNumber = "";
                        String billType = "";
                        ReceiptDetails receiptData = new ReceiptDetails(
                                "Mobile Top Up",
                                NumberFormat.getCurrencyInstance(Locale.US).format(currentTopUpAmount),
                                "TOPUP_REF_" + System.currentTimeMillis(), // Example reference
                                new Date().getTime(),
                                true, // Assuming success for now, you might have actual success/fail logic
                                "Payment for " + billType + " to " + accountNumber, null, "Successfully topped up your account."
                                // Add other fields to ReceiptDetails as needed
                        );
                        showReceipt(receiptData);
                        // --- END MODIFICATION ---

                    } else {
                        showError("Please enter a valid amount");
                    }
                } catch (NumberFormatException e) {
                    showError("Please enter a valid number");
                }
            } else {
                showError("Please enter an amount");
            }
        });
    }

    private void showReceipt(ReceiptDetails details) {
        CustomDialog dialogFragment = CustomDialog.newInstance(details);
        dialogFragment.setReceiptDialogListener(this);
        dialogFragment.show(getSupportFragmentManager(), CustomDialog.TAG);
    }

    @Override
    public void onReceiptDialogClose(DialogFragment dialog) {
        // This is called when the receipt dialog is closed
        // Now, set the result and finish
        Intent resultIntent = new Intent();
        resultIntent.putExtra("amount", currentTopUpAmount);
        resultIntent.putExtra("description", "Top-up of " + NumberFormat.getCurrencyInstance(Locale.US).format(currentTopUpAmount));
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}