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

        ImageView statusIcon = view.findViewById(R.id.statusIcon);
        TextView statusText = view.findViewById(R.id.statusText);
        TextView transactionTypeText = view.findViewById(R.id.paymentMethod);
        TextView amountText = view.findViewById(R.id.amount);
        TextView descriptionText = view.findViewById(R.id.description);
        TextView partyNameText = view.findViewById(R.id.sender);
        TextView referenceNumberText = view.findViewById(R.id.referenceNumber);
        TextView paymentTimeText = view.findViewById(R.id.paymentTime);
        Button closeButton = view.findViewById(R.id.closeButton);

        Bundle args = getArguments();
        if (args != null) {
            ReceiptDetails details = args.getParcelable(ARG_RECEIPT_DETAILS);
            if (details != null) {
                // Status
                if (details.isSuccess()) {
                    statusIcon.setImageResource(R.drawable.success); // Ensure you have this drawable
                    statusText.setText(R.string.payment_success); // Ensure you have this string
                } else {
                    statusIcon.setImageResource(R.drawable.x); // Ensure you have this drawable
                    statusText.setText(R.string.payment_failed); // Ensure you have this string
                }

                // Transaction Type (Title of the receipt)
                if (details.getTransactionType() != null && !details.getTransactionType().isEmpty()) {
                    transactionTypeText.setText(details.getTransactionType());
                    transactionTypeText.setVisibility(View.VISIBLE);
                } else {
                    transactionTypeText.setVisibility(View.GONE);
                }

                // Amount
                if (details.getAmount() != null && !details.getAmount().isEmpty()) {
                    amountText.setText(details.getAmount()); // Assuming amount is pre-formatted
                    amountText.setVisibility(View.VISIBLE);
                } else {
                    amountText.setVisibility(View.GONE);
                }

                // Description
                if (details.getDescription() != null && !details.getDescription().isEmpty()) {
                    descriptionText.setText(details.getDescription());
                    descriptionText.setVisibility(View.VISIBLE);
                } else {
                    descriptionText.setVisibility(View.GONE);
                }


                // Party Name (Recipient or Biller)
                String partyName = null;
                String partyLabel = "";
                if (details.getRecipientName() != null && !details.getRecipientName().isEmpty()) {
                    partyName = details.getRecipientName();
                    partyLabel = "To: "; // Or "Recipient: "
                }

                if (partyName != null) {
                    partyNameText.setText(partyLabel + partyName);
                    partyNameText.setVisibility(View.VISIBLE);
                } else {
                    partyNameText.setVisibility(View.GONE);
                }


                // Reference Number
                if (details.getReferenceNumber() != null && !details.getReferenceNumber().isEmpty()) {
                    referenceNumberText.setText("Ref: " + details.getReferenceNumber());
                    referenceNumberText.setVisibility(View.VISIBLE);
                } else {
                    referenceNumberText.setVisibility(View.GONE);
                }

                // Payment Time
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
                paymentTimeText.setText("Date: " + sdf.format(new Date(details.getTimestamp())));
                paymentTimeText.setVisibility(View.VISIBLE); // Time is usually always present
            }
        }

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
        // Optional: Remove the default dialog title bar for a cleaner look if your layout has its own title
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
            // Set dialog width to a percentage of the screen width or a fixed value
            // This helps make the dialog look more like a card
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90); // 90% of screen width
            // int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);

            // Optional: Add rounded corners (requires a background drawable with corners)
            // dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_background);
        }
    }
}