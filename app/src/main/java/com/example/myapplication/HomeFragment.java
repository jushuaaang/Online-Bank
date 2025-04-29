package com.example.myapplication;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private DataBaseActivity dbHelper;
    private TextView balanceAmountText;
    private RecyclerView transactionsRecyclerView;
    private TransactionAdapter adapter;
    private static final int TOP_UP_REQUEST_CODE = 1;
    private static final int SEND_REQUEST_CODE = 2;
    private static final int PAY_BILLS_REQUEST_CODE = 3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        dbHelper = new DataBaseActivity(requireContext());
        balanceAmountText = view.findViewById(R.id.balanceAmountText);
        transactionsRecyclerView = view.findViewById(R.id.transactionsRecyclerView);
        LinearLayout topUpLayout = view.findViewById(R.id.topUpActionLayout);
        LinearLayout sendLayout = view.findViewById(R.id.sendActionLayout);
        LinearLayout payBillsLayout = view.findViewById(R.id.payBillsActionLayout);

        setupClickListeners(topUpLayout, sendLayout, payBillsLayout);
        setupRecyclerView();
        refreshDashboard();

        return view;
    }

    private void setupRecyclerView() {
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, true));
        adapter = new TransactionAdapter(new ArrayList<>());
        transactionsRecyclerView.setAdapter(adapter);

        int spacing = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        transactionsRecyclerView.addItemDecoration(new SpacingItemDecoration(spacing));
    }

    private void setupClickListeners(View... views) {
        for (View v : views) {
            v.setOnClickListener(view -> {
                Intent intent;
                int requestCode;

                if (view.getId() == R.id.topUpActionLayout) {
                    intent = new Intent(requireActivity(), TopUpActivity.class);
                    requestCode = TOP_UP_REQUEST_CODE;
                } else if (view.getId() == R.id.sendActionLayout) {
                    intent = new Intent(requireActivity(), SendActivity.class);
                    requestCode = SEND_REQUEST_CODE;
                } else {
                    intent = new Intent(requireActivity(), PayBillsActivity.class);
                    requestCode = PAY_BILLS_REQUEST_CODE;
                }

                intent.putExtra("currentBalance", dbHelper.getBalance());
                startActivityForResult(intent, requestCode);
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            double amount = data.getDoubleExtra("amount", 0);
            String description = data.getStringExtra("description");
            double currentBalance = dbHelper.getBalance();

            if (amount <= 0) {
                showToast("Invalid amount");
                return;
            }

            dbHelper.beginTransaction();
            try {
                if (requestCode == TOP_UP_REQUEST_CODE) {
                    dbHelper.updateBalance(currentBalance + amount);
                    dbHelper.addTransaction(amount, "TOP_UP", description);
                    showToast("Top up successful");
                } else {
                    if (amount > currentBalance) {
                        showToast("Insufficient balance");
                        return;
                    }
                    dbHelper.updateBalance(currentBalance - amount);
                    String type = (requestCode == SEND_REQUEST_CODE) ? "SEND" : "BILL_PAYMENT";
                    dbHelper.addTransaction(-amount, type, description);
                    showToast(type.equals("SEND") ? "Money sent" : "Bill paid");
                }
                dbHelper.setTransactionSuccessful();
            } finally {
                dbHelper.endTransaction();
            }

            refreshDashboard();
        }
    }

    private void refreshDashboard() {
        balanceAmountText.setText(String.format("$%.2f", dbHelper.getBalance()));

        Cursor cursor = dbHelper.getAllTransactions();
        List<Transaction> transactions = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                transactions.add(new Transaction(
                        cursor.getInt(cursor.getColumnIndexOrThrow("_id")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),
                        cursor.getString(cursor.getColumnIndexOrThrow("type")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getString(cursor.getColumnIndexOrThrow("date"))
                ));
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter.updateTransactions(transactions);
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshDashboard();
    }
}
