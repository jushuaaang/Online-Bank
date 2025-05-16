package com.example.myapplication;

import android.os.Parcel;
import android.os.Parcelable;

public class ReceiptDetails implements Parcelable {
    private final String transactionType; // e.g., "Mobile Top Up", "Send Money", "Bill Payment"
    private final String amount;          // Formatted currency string
    private final String referenceNumber;
    private final long timestamp;
    private final boolean success;
    private final String description;     // e.g., "Successfully topped up...", "Payment for Electricity..."
    private String sender;   // For Send Money

    public ReceiptDetails(String transactionType, String amount, String referenceNumber,
                          long timestamp, boolean success, String s, Object o, String description) {
        this.transactionType = transactionType;
        this.amount = amount;
        this.referenceNumber = referenceNumber;
        this.timestamp = timestamp;
        this.success = success;
        this.description = description;
        this.sender = sender;
    }

    // Getters
    public String getTransactionType() { return transactionType; }
    public String getAmount() { return amount; }
    public String getReferenceNumber() { return referenceNumber; }
    public long getTimestamp() { return timestamp; }
    public boolean isSuccess() { return success; }
    public String getDescription() { return description; }
    public String getRecipientName() { return sender; }



    // --- Parcelable Implementation ---
    protected ReceiptDetails(Parcel in) {
        transactionType = in.readString();
        amount = in.readString();
        referenceNumber = in.readString();
        timestamp = in.readLong();
        success = in.readByte() != 0;
        description = in.readString();
        sender = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(transactionType);
        dest.writeString(amount);
        dest.writeString(referenceNumber);
        dest.writeLong(timestamp);
        dest.writeByte((byte) (success ? 1 : 0));
        dest.writeString(description);
        dest.writeString(sender);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ReceiptDetails> CREATOR = new Creator<ReceiptDetails>() {
        @Override
        public ReceiptDetails createFromParcel(Parcel in) {
            return new ReceiptDetails(in);
        }

        @Override
        public ReceiptDetails[] newArray(int size) {
            return new ReceiptDetails[size];
        }
    };
}