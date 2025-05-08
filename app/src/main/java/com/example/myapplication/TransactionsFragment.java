package com.example.myapplication;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TransactionsFragment extends Fragment {
    private DataBaseActivity dbHelper;
    private RecyclerView transactionsRecyclerView;
    private TransactionAdapter adapter;

    public TransactionsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);

        dbHelper = new DataBaseActivity(requireContext());

        transactionsRecyclerView = view.findViewById(R.id.transactionsRecyclerView);

        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TransactionAdapter(requireContext(), new ArrayList<Transaction>());
        transactionsRecyclerView.setAdapter(adapter);

        loadTransactions();

        return view;
    }

    private void loadTransactions() {
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
            System.out.println("No transactions found in TransactionsFragment");
        }

        adapter.updateTransactions(transactions);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}