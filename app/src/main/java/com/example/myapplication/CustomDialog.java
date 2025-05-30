package com.example.myapplication;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
// import androidx.core.content.ContextCompat; // No longer needed for getString with format args
import androidx.fragment.app.DialogFragment;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CustomDialog extends DialogFragment {

    public static final String TAG = "ReceiptDialog";
    private static final String ARG_RECEIPT_DETAILS = "receipt_details";

    private ReceiptDialogListener receiptDialogListener;

    // Interface for callbacks
    public interface ReceiptDialogListener {
        void onReceiptDialogClose(DialogFragment dialog);
    }

    public static CustomDialog newInstance(ReceiptDetails details) {
        CustomDialog fragment = new CustomDialog();
        Bundle args = new Bundle();
        args.putParcelable(ARG_RECEIPT_DETAILS, details);
        fragment.setArguments(args);
        return fragment;
    }

    public void setReceiptDialogListener(ReceiptDialogListener listener) {
        this.receiptDialogListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_receipt, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- Find Views ---
        ImageView statusIcon = view.findViewById(R.id.statusIcon);
        TextView statusText = view.findViewById(R.id.statusText);
        TextView transactionTypeText = view.findViewById(R.id.paymentMethod);
        TextView amountText = view.findViewById(R.id.amount);
        TextView descriptionText = view.findViewById(R.id.description);
        TextView senderNameText = view.findViewById(R.id.sender); // TextView for the Sender's Name
        TextView recipientNameText = view.findViewById(R.id.recipient); // TextView for the Recipient's Name (ensure this ID exists in your XML if you need it)
        TextView referenceNumberText = view.findViewById(R.id.referenceNumber);
        TextView paymentTimeText = view.findViewById(R.id.paymentTime);
        Button closeButton = view.findViewById(R.id.closeButton);

        Bundle args = getArguments();
        if (args != null) {
            ReceiptDetails details = args.getParcelable(ARG_RECEIPT_DETAILS);
            if (details != null) {
                // --- Populate Status ---
                if (details.isSuccess()) {
                    statusIcon.setImageResource(R.drawable.success);
                    statusText.setText(getString(R.string.payment_success)); // Correct: Fragment's getString
                } else {
                    statusIcon.setImageResource(R.drawable.x);
                    statusText.setText(getString(R.string.payment_failed));  // Correct: Fragment's getString
                }

                // --- Populate Transaction Type ---
                if (details.getTransactionType() != null && !details.getTransactionType().isEmpty()) {
                    transactionTypeText.setText(details.getTransactionType());
                    transactionTypeText.setVisibility(View.VISIBLE);
                } else {
                    transactionTypeText.setVisibility(View.GONE);
                }

                // --- Populate Amount ---
                if (details.getAmount() != null && !details.getAmount().isEmpty()) {
                    amountText.setText(details.getAmount());
                    amountText.setVisibility(View.VISIBLE);
                } else {
                    amountText.setVisibility(View.GONE);
                }

                // --- Populate Description ---
                if (details.getDescription() != null && !details.getDescription().isEmpty()) {
                    descriptionText.setText(details.getDescription());
                    descriptionText.setVisibility(View.VISIBLE);
                } else {
                    descriptionText.setVisibility(View.GONE);
                }

                // --- Populate SENDER Name ---
                if (details.getSenderUsername() != null && !details.getSenderUsername().isEmpty()) {
                    // CORRECTED: Use Fragment's getString for formatted strings
                    senderNameText.setText(getString(R.string.receipt_from_label, details.getSenderUsername()));
                    senderNameText.setVisibility(View.VISIBLE);
                } else {
                    // CORRECTED: Use Fragment's getString for formatted strings
                    senderNameText.setText(getString(R.string.receipt_from_label, "Unknown"));
                    senderNameText.setVisibility(View.VISIBLE);
                }

                // --- Populate RECIPIENT Name ---
                if (recipientNameText != null) {
                    if (details.getRecipientName() != null && !details.getRecipientName().isEmpty()) {
                        // CORRECTED: Use Fragment's getString for formatted strings
                        recipientNameText.setText(getString(R.string.receipt_to_label, details.getRecipientName()));
                        recipientNameText.setVisibility(View.VISIBLE);
                    } else {
                        recipientNameText.setVisibility(View.GONE);
                    }
                }

                // --- Populate Reference Number ---
                if (details.getReferenceNumber() != null && !details.getReferenceNumber().isEmpty()) {
                    // CORRECTED: Use Fragment's getString for formatted strings
                    referenceNumberText.setText(getString(R.string.receipt_ref_label, details.getReferenceNumber()));
                    referenceNumberText.setVisibility(View.VISIBLE);
                } else {
                    referenceNumberText.setVisibility(View.GONE);
                }

                // --- Populate Payment Time ---
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
                // CORRECTED: Use Fragment's getString for formatted strings
                paymentTimeText.setText(getString(R.string.receipt_date_label, sdf.format(new Date(details.getTimestamp()))));
                paymentTimeText.setVisibility(View.VISIBLE);
            }
        }

        // --- Close Button ---
        closeButton.setOnClickListener(v -> {
            if (receiptDialogListener != null) {
                receiptDialogListener.onReceiptDialogClose(this);
            }
            dismiss(); // Dismiss the dialog
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            // dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_background); // Optional
        }
    }
}