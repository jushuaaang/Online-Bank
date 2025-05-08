package com.example.myapplication;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

    private ActivityResultLauncher<Intent> topUpLauncher;
    private ActivityResultLauncher<Intent> sendLauncher;
    private ActivityResultLauncher<Intent> payBillsLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        topUpLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> handleActivityResult("TOP_UP", result)
        );

        sendLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> handleActivityResult("SEND", result)
        );

        payBillsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> handleActivityResult("BILL_PAYMENT", result)
        );
    }

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
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        adapter = new TransactionAdapter(requireContext(), new ArrayList<>());
        transactionsRecyclerView.setAdapter(adapter);
        transactionsRecyclerView.addItemDecoration(new SpacingItemDecoration(8));
    }

    private void setupClickListeners(View... views) {
        for (View v : views) {
            v.setOnClickListener(view -> {
                Intent intent;
                ActivityResultLauncher<Intent> launcher;

                if (view.getId() == R.id.topUpActionLayout) {
                    intent = new Intent(requireActivity(), TopUpActivity.class);
                    launcher = topUpLauncher;
                } else if (view.getId() == R.id.sendActionLayout) {
                    intent = new Intent(requireActivity(), SendActivity.class);
                    launcher = sendLauncher;
                } else {
                    intent = new Intent(requireActivity(), PayBillsActivity.class);
                    launcher = payBillsLauncher;
                }

                intent.putExtra("currentBalance", dbHelper.getBalance());
                launcher.launch(intent);
            });
        }
    }

    private void handleActivityResult(String type, ActivityResult result) {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Intent data = result.getData();
            double amount = data.getDoubleExtra("amount", 0);
            String description = data.getStringExtra("description");
            double currentBalance = dbHelper.getBalance();

            if (amount <= 0) {
                showToast("Invalid amount");
                return;
            }

            dbHelper.beginTransaction();
            try {
                if (type.equals("TOP_UP")) {
                    dbHelper.updateBalance(currentBalance + amount);
                    dbHelper.addTransaction(amount, "TOP_UP", description);
                    showToast("Top up successful");
                } else if (type.equals("SEND") || type.equals("BILL_PAYMENT")) {
                    if (amount > currentBalance) {
                        showToast("Insufficient balance");
                        return;
                    }
                    dbHelper.updateBalance(currentBalance - amount);
                    dbHelper.addTransaction(-amount, type, description);
                    showToast(type.equals("SEND") ? "Money sent" : "Bill paid");
                }
                dbHelper.setTransactionSuccessful();
            } catch (Exception e) {
                showToast("Transaction failed: " + e.getMessage());
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

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    transactions.add(new Transaction(
                            cursor.getInt(cursor.getColumnIndexOrThrow("_id")),
                            cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),
                            cursor.getString(cursor.getColumnIndexOrThrow("type")),
                            cursor.getString(cursor.getColumnIndexOrThrow("description")),
                            cursor.getString(cursor.getColumnIndexOrThrow("date"))
                    ));
                }
            } finally {
                cursor.close();
            }
        }

        if (transactions.isEmpty()) {
            showToast("No transactions found");
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