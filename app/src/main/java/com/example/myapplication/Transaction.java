package com.example.myapplication;

public class Transaction {
    private int id;
    private double amount;
    private String type;
    private String description;
    private String date;

    public Transaction(int id, double amount, String type, String description, String date) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.date = date;
    }

    // Getters
    public int getId() { return id; }
    public double getAmount() { return amount; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
}