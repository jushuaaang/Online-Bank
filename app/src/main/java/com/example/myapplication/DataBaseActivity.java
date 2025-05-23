package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import androidx.annotation.NonNull;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DataBaseActivity extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "BankingApp.db";
    private static final int DATABASE_VERSION = 1;

    // Tables
    private static final String TABLE_TRANSACTIONS = "transactions";
    private static final String TABLE_BALANCE = "balance";

    // Common columns
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_BALANCE = "balance";

    // Create table statements
    private static final String CREATE_TRANSACTIONS_TABLE = "CREATE TABLE " + TABLE_TRANSACTIONS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_AMOUNT + " REAL NOT NULL,"
            + COLUMN_TYPE + " TEXT NOT NULL,"
            + COLUMN_DESCRIPTION + " TEXT NOT NULL,"
            + COLUMN_DATE + " TEXT NOT NULL"
            + ")";

    private static final String CREATE_BALANCE_TABLE = "CREATE TABLE " + TABLE_BALANCE + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_BALANCE + " REAL NOT NULL"
            + ")";

    private final SQLiteDatabase db;

    public DataBaseActivity(@NonNull Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TRANSACTIONS_TABLE);
            db.execSQL(CREATE_BALANCE_TABLE);

            // Initialize balance with default amount
            ContentValues values = new ContentValues();
            values.put(COLUMN_BALANCE, 5600.00);
            db.insert(TABLE_BALANCE, null, values);
        } catch (Exception e) {
            Log.e("DataBaseActivity", "Error creating database: " + e.getMessage(), e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_BALANCE);
            onCreate(db);
        } catch (Exception e) {
            Log.e("DataBaseActivity", "Error upgrading database: " + e.getMessage(), e);
        }
    }

    public void beginTransaction() {
        if (db != null) {
            db.beginTransaction();
        }
    }

    public void setTransactionSuccessful() {
        if (db != null) {
            db.setTransactionSuccessful();
        }
    }

    public void endTransaction() {
        if (db != null) {
            db.endTransaction();
        }
    }

    public double getBalance() {
        double balance = 0.0;
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_BALANCE, new String[]{COLUMN_BALANCE},
                    null, null, null, null,
                    COLUMN_ID + " DESC", "1");

            if (cursor != null && cursor.moveToFirst()) {
                balance = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_BALANCE));
            }
        } catch (Exception e) {
            Log.e("DataBaseActivity", "Error fetching balance: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return balance;
    }

    public void updateBalance(double newBalance) {
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_BALANCE, newBalance);
            db.update(TABLE_BALANCE, values, COLUMN_ID + " = (SELECT MAX(" + COLUMN_ID + ") FROM " + TABLE_BALANCE + ")", null);
        } catch (Exception e) {
            Log.e("DataBaseActivity", "Error updating balance: " + e.getMessage(), e);
        }
    }

    public void addTransaction(double amount, String type, String description) {
        if (description == null || description.trim().isEmpty()) {
            description = "No description provided"; // Default value
        }

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_AMOUNT, amount);
            values.put(COLUMN_TYPE, type);
            values.put(COLUMN_DESCRIPTION, description);
            values.put(COLUMN_DATE, getCurrentDateTime());
            db.insertOrThrow(TABLE_TRANSACTIONS, null, values);
        } catch (Exception e) {
            Log.e("DataBaseActivity", "Error adding transaction: " + e.getMessage(), e);
        }
    }

    public Cursor getAllTransactions() {
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_TRANSACTIONS, null, null, null, null, null,
                    COLUMN_ID + " DESC");
        } catch (Exception e) {
            Log.e("DataBaseActivity", "Error fetching transactions: " + e.getMessage(), e);
        }
        return cursor;
    }

    private String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    @Override
    public synchronized void close() {
        if (db != null) {
            db.close();
        }
        super.close();
    }
}