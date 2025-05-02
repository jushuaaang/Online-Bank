    package com.example.myapplication;

    import android.content.Context;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ImageView;
    import android.widget.TextView;
    import androidx.annotation.NonNull;
    import androidx.core.content.ContextCompat;
    import androidx.recyclerview.widget.RecyclerView;
    import java.text.NumberFormat;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Locale;

    public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
        private final Context context;
        private final List<Transaction> transactions;
        private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

        public TransactionAdapter(Context context, List<Transaction> transactions) {
            this.context = context;
            this.transactions = transactions;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Transaction transaction = transactions.get(position);

            // Set the icon based on transaction type
            int iconResource = getTransactionIcon(transaction.getDescription());
            holder.iconView.setImageResource(iconResource);

            // Set icon tint based on transaction type
            int tintColor = getTransactionIconTint(transaction.getDescription());
            //holder.iconView.setColorFilter(tintColor);

            // Set transaction details
            holder.descriptionView.setText(transaction.getDescription());
            holder.dateView.setText(transaction.getDate());

            // Format and set amount
            String amount = currencyFormat.format(transaction.getAmount());
            if (transaction.getAmount() < 0) {
                holder.amountView.setTextColor(ContextCompat.getColor(context, R.color.expense_red));
            } else {
                holder.amountView.setTextColor(ContextCompat.getColor(context, R.color.income_green));
            }
            holder.amountView.setText(amount);
        }

        private int getTransactionIcon(String description) {
            if (description.contains("Electricity")) {
                return R.drawable.electricity;
            } else if (description.contains("Water")) {
                return R.drawable.water;
            } else if (description.contains("Gas")) {
                return R.drawable.gas;
            } else if (description.contains("Internet")) {
                return R.drawable.internet;
            } else if (description.contains("Spotify")) {
                return R.drawable.spotify;
            } else if (description.contains("Netflix")) {
                return R.drawable.netflix;
            } else if (description.contains("Send")) {
                return R.drawable.send_money;
            } else if (description.contains("Received")) {
                return R.drawable.receive_money;
            } else if (description.contains("Phone") || description.contains("Call")) {
                return R.drawable.phone;
            }else {
                return R.drawable.ic_time;
            }
        }

        private int getTransactionIconTint(String description) {
            if (description.contains("Internet")) {
                return ContextCompat.getColor(context, R.color.internet_blue);
            } else if (description.contains("Send") || description.contains("Electricity") ||
                    description.contains("Water")) {
                return ContextCompat.getColor(context, R.color.blue);
            } else if (description.contains("Received")) {
                return ContextCompat.getColor(context, R.color.income_green);
            } else {
                return ContextCompat.getColor(context, R.color.gray);
            }
        }

        @Override
        public int getItemCount() {
            return transactions.size();
        }

        // ✅ Add this missing method to allow data refresh
        public void updateTransactions(List<Transaction> newTransactions) {
            transactions.clear();
            transactions.addAll(newTransactions);
            notifyDataSetChanged();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView iconView;
            TextView descriptionView;
            TextView dateView;
            TextView amountView;

            ViewHolder(View itemView) {
                super(itemView);
                iconView = itemView.findViewById(R.id.transactionIcon);
                descriptionView = itemView.findViewById(R.id.transactionDescription);
                dateView = itemView.findViewById(R.id.transactionDate);
                amountView = itemView.findViewById(R.id.transactionAmount);
            }
        }
    }
