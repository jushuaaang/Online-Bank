package com.example.myapplication;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import java.util.Date; // << IMPORT ADDED

public class ReceiptDetails implements Parcelable {
    private final String transactionType;
    private final String amount;
    private final String referenceNumber;
    private final long timestamp;
    private final boolean success;
    private final String description;
    private final String senderUsername;
    private final String recipientName; // Made final as it's set in constructor and read from parcel

    // Main constructor
    public ReceiptDetails(String transactionType, String amount, String referenceNumber,
                          long timestamp, boolean success, String description,
                          String senderUsername, @Nullable String recipientName) {
        this.transactionType = transactionType;
        this.amount = amount;
        this.referenceNumber = referenceNumber;
        this.timestamp = timestamp;
        this.success = success;
        this.description = description;
        this.senderUsername = senderUsername;
        this.recipientName = recipientName;
    }

    // Getters
    public String getTransactionType() { return transactionType; }
    public String getAmount() { return amount; }
    public String getReferenceNumber() { return referenceNumber; }
    public long getTimestamp() { return timestamp; }
    public boolean isSuccess() { return success; }
    public String getDescription() { return description; }
    public String getSenderUsername() { return senderUsername; }
    @Nullable
    public String getRecipientName() { return recipientName; }

    // --- Parcelable Implementation ---

    // Constructor to read from Parcel
    protected ReceiptDetails(Parcel in) {
        transactionType = in.readString();
        amount = in.readString();
        referenceNumber = in.readString();
        timestamp = in.readLong();
        success = in.readByte() != 0; // success == 1 if true
        description = in.readString();
        senderUsername = in.readString();
        recipientName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(transactionType);
        dest.writeString(amount);
        dest.writeString(referenceNumber);
        dest.writeLong(timestamp);
        dest.writeByte((byte) (success ? 1 : 0));
        dest.writeString(description);
        dest.writeString(senderUsername);
        dest.writeString(recipientName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ReceiptDetails> CREATOR = new Creator<ReceiptDetails>() {
        @Override
        public ReceiptDetails createFromParcel(Parcel in) {
            // Use the constructor that reads from the Parcel
            return new ReceiptDetails(in);
        }

        @Override
        public ReceiptDetails[] newArray(int size) {
            return new ReceiptDetails[size];
        }
    };
}