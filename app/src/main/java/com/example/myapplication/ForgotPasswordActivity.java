package com.example.myapplication; // << REPLACE with your actual package name

import android.content.Intent; // Added for potential navigation to LoginActivity
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log; // Added for better logging
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordActivity"; // For logging

    private EditText usernameEditText;
    private Button checkUserButton;
    private LinearLayout enterUsernameLayout;
    private LinearLayout securityQuestionLayout;
    private TextView securityQuestionTextView;
    private EditText securityAnswerEditText;
    private Button verifyAnswerButton;
    private LinearLayout resetPasswordLayout;
    private EditText newPasswordEditText;
    private EditText confirmNewPasswordEditText;
    private Button resetPasswordButton;

    private String currentUsernameForReset; // Store the username once verified

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        enterUsernameLayout = findViewById(R.id.layoutEnterUsername);
        usernameEditText = findViewById(R.id.editTextForgotUsername);
        checkUserButton = findViewById(R.id.buttonCheckUser);

        securityQuestionLayout = findViewById(R.id.layoutSecurityQuestion);
        securityQuestionTextView = findViewById(R.id.textViewSecurityQuestionDisplay);
        securityAnswerEditText = findViewById(R.id.editTextSecurityAnswerForgot);
        verifyAnswerButton = findViewById(R.id.buttonVerifyAnswer);

        resetPasswordLayout = findViewById(R.id.layoutResetPassword);
        newPasswordEditText = findViewById(R.id.editTextNewPassword);
        confirmNewPasswordEditText = findViewById(R.id.editTextConfirmNewPassword);
        resetPasswordButton = findViewById(R.id.buttonConfirmResetPassword);

        // Initially, show only the username input layout
        enterUsernameLayout.setVisibility(View.VISIBLE);
        securityQuestionLayout.setVisibility(View.GONE);
        resetPasswordLayout.setVisibility(View.GONE);

        checkUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findAccountAndShowQuestion();
            }
        });

        verifyAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifySecurityAnswerAndShowReset();
            }
        });

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performPasswordReset();
            }
        });
    }

    private void findAccountAndShowQuestion() {
        String username = usernameEditText.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Please enter username or email");
            usernameEditText.requestFocus();
            return;
        }

        String securityQuestion = UserCredentialManager.getUserSecurityQuestion(this, username);

        if (securityQuestion != null) {
            currentUsernameForReset = username;
            securityQuestionTextView.setText(securityQuestion);

            enterUsernameLayout.setVisibility(View.GONE);
            securityQuestionLayout.setVisibility(View.VISIBLE);
            resetPasswordLayout.setVisibility(View.GONE); // Ensure reset layout is hidden
            securityAnswerEditText.setText(""); // Clear previous answer
            securityAnswerEditText.requestFocus();
        } else {
            usernameEditText.setError("Account not found or security question not set up.");
            usernameEditText.requestFocus();
            Toast.makeText(this, "Account not found or security question not set up for this user.", Toast.LENGTH_LONG).show();
            // Ensure other layouts remain hidden or reset
            enterUsernameLayout.setVisibility(View.VISIBLE);
            securityQuestionLayout.setVisibility(View.GONE);
            resetPasswordLayout.setVisibility(View.GONE);
        }
    }

    // --- ADDED METHOD ---
    private void verifySecurityAnswerAndShowReset() {
        String answer = securityAnswerEditText.getText().toString().trim();

        if (TextUtils.isEmpty(answer)) {
            securityAnswerEditText.setError("Please enter your answer");
            securityAnswerEditText.requestFocus();
            return;
        }

        if (currentUsernameForReset == null || currentUsernameForReset.isEmpty()) {
            Toast.makeText(this, "Error: Username not found. Please start over.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "verifySecurityAnswerAndShowReset: currentUsernameForReset is null or empty");
            // Reset to the first step
            enterUsernameLayout.setVisibility(View.VISIBLE);
            securityQuestionLayout.setVisibility(View.GONE);
            resetPasswordLayout.setVisibility(View.GONE);
            usernameEditText.setText("");
            usernameEditText.requestFocus();
            return;
        }

        if (UserCredentialManager.verifySecurityAnswer(this, currentUsernameForReset, answer)) {
            // Answer is correct
            securityQuestionLayout.setVisibility(View.GONE);
            enterUsernameLayout.setVisibility(View.GONE); // Ensure username layout is also hidden
            resetPasswordLayout.setVisibility(View.VISIBLE);
            newPasswordEditText.setText(""); // Clear previous passwords
            confirmNewPasswordEditText.setText("");
            newPasswordEditText.requestFocus();
            Toast.makeText(this, "Answer verified. Please set a new password.", Toast.LENGTH_SHORT).show();
        } else {
            // Answer is incorrect
            securityAnswerEditText.setError("Incorrect answer. Please try again.");
            securityAnswerEditText.requestFocus();
            securityAnswerEditText.selectAll();
            Toast.makeText(this, "Security answer is incorrect.", Toast.LENGTH_SHORT).show();
        }
    }

    // --- ADDED METHOD ---
    private void performPasswordReset() {
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmNewPassword = confirmNewPasswordEditText.getText().toString().trim();

        // Basic validation
        if (TextUtils.isEmpty(newPassword)) {
            newPasswordEditText.setError("New password cannot be empty");
            newPasswordEditText.requestFocus();
            return;
        }
        // You can add more password strength rules here (e.g., length)
        if (newPassword.length() < 6) { // Example minimum length
            newPasswordEditText.setError("Password must be at least 6 characters");
            newPasswordEditText.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(confirmNewPassword)) {
            confirmNewPasswordEditText.setError("Please confirm your new password");
            confirmNewPasswordEditText.requestFocus();
            return;
        }
        if (!newPassword.equals(confirmNewPassword)) {
            confirmNewPasswordEditText.setError("Passwords do not match");
            // Clear both fields on mismatch for better UX
            newPasswordEditText.setText("");
            confirmNewPasswordEditText.setText("");
            newPasswordEditText.requestFocus();
            return;
        }

        if (currentUsernameForReset == null || currentUsernameForReset.isEmpty()) {
            Toast.makeText(this, "Error: Session expired or username not found. Please start over.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "performPasswordReset: currentUsernameForReset is null or empty");
            // Reset to the first step
            enterUsernameLayout.setVisibility(View.VISIBLE);
            securityQuestionLayout.setVisibility(View.GONE);
            resetPasswordLayout.setVisibility(View.GONE);
            usernameEditText.setText("");
            usernameEditText.requestFocus();
            return;
        }

        if (UserCredentialManager.resetPassword(this, currentUsernameForReset, newPassword)) {
            Toast.makeText(this, "Password reset successfully! Please log in with your new password.", Toast.LENGTH_LONG).show();

            // Navigate to LoginActivity
            Intent intent = new Intent(ForgotPasswordActivity.this, LogInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            startActivity(intent);
            finish(); // Close this activity
        } else {
            Toast.makeText(this, "Failed to reset password. Please try again or contact support.", Toast.LENGTH_SHORT).show();
            // Consider what to do here - maybe allow another attempt or guide the user
        }
    }
}