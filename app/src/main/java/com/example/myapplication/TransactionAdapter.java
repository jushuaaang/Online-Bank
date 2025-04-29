package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Transaction> transactionList;

    // Constructor
    public TransactionAdapter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView iconView;
        public TextView merchantName;
        public TextView dateView;
        public TextView amountView;

        public ViewHolder(View view) {
            super(view);
            // Initialize views
            iconView = view.findViewById(R.id.transactionIcon);
            merchantName = view.findViewById(R.id.merchantName);
            dateView = view.findViewById(R.id.transactionDate);
            amountView = view.findViewById(R.id.transactionAmount);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get transaction at this position
        Transaction transaction = transactionList.get(position);

        // Set the icon based on transaction type
        switch (transaction.getType()) {
            case "TOP_UP":
                holder.iconView.setImageResource(R.drawable.top_up);
                break;
            case "SEND":
                holder.iconView.setImageResource(R.drawable.ic_send);
                break;
            case "BILL_PAYMENT":
                holder.iconView.setImageResource(R.drawable.paybills);
                break;
        }

        // Set merchant name/description
        holder.merchantName.setText(transaction.getDescription());

        // Set date
        holder.dateView.setText(transaction.getDate());

        // Set amount with proper formatting
        double amount = transaction.getAmount();
        String amountText = String.format("$%.2f", Math.abs(amount));

        // Add + or - sign and set color
        if (amount > 0) {
            amountText = "+" + amountText;
            holder.amountView.setTextColor(holder.itemView.getContext()
                    .getResources().getColor(android.R.color.holo_green_dark));
        } else {
            amountText = "-" + amountText;
            holder.amountView.setTextColor(holder.itemView.getContext()
                    .getResources().getColor(android.R.color.holo_red_dark));
        }
        holder.amountView.setText(amountText);
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    // Method to update transaction list
    public void updateTransactions(List<Transaction> newTransactions) {
        this.transactionList = newTransactions;
        notifyDataSetChanged();
    }
}