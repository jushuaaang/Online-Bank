package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LogInActivity extends AppCompatActivity {

    private EditText usernameOrEmailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView signUpTextView;
    private TextView forgotPasswordTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (UserCredentialManager.getLoggedInUser(this) != null) {
            goToMainActivity();
            return;
        }

        setContentView(R.layout.activity_log_in);

        usernameOrEmailEditText = findViewById(R.id.editTextLoginUsernameOrEmail);
        passwordEditText = findViewById(R.id.editTextLoginPassword);
        loginButton = findViewById(R.id.buttonLogin);
        signUpTextView = findViewById(R.id.textViewSignUpLink);
        forgotPasswordTextView = findViewById(R.id.textViewForgotPassword);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processLogin();
            }
        });

        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LogInActivity.this, SignUpActivity.class));
            }
        });

        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LogInActivity.this, ForgotPasswordActivity.class));
            }
        });
    }

    private void processLogin() {
        String usernameOrEmail = usernameOrEmailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(usernameOrEmail)) {
            usernameOrEmailEditText.setError("Username or Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            return;
        }

        String loggedInUserIdentifier = UserCredentialManager.getLoggedInUserIdentifier(this, usernameOrEmail, password);

        if (loggedInUserIdentifier != null) {
            String loggedInUsername = UserCredentialManager.getUsernameByIdentifier(this, loggedInUserIdentifier);
            String loggedInEmail = UserCredentialManager.getEmailByIdentifier(this, loggedInUserIdentifier);

            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            if (loggedInUsername != null) {
                editor.putString("userName", loggedInUsername);
            } else {
                editor.putString("userName", usernameOrEmail); // Fallback if specific username fetch fails
            }
            if (loggedInEmail != null) {
                editor.putString("userEmail", loggedInEmail);
            } else {
                // If the input was an email, store it. Otherwise, you might need another way to get email.
                if (usernameOrEmail.contains("@")) {
                    editor.putString("userEmail", usernameOrEmail);
                } else {
                    // If login was by username and email isn't directly available here,
                    // you might leave it or fetch it if UserCredentialManager supports it
                    editor.remove("userEmail"); // Or set to a default/placeholder
                }
            }
            editor.apply();

            Toast.makeText(LogInActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
            goToMainActivity();
        } else {
            Toast.makeText(LogInActivity.this, "Invalid username/email or password", Toast.LENGTH_SHORT).show();
        }
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LogInActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}