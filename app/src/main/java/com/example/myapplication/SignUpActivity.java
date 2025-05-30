package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class SignUpActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Spinner securityQuestionSpinner;
    private EditText securityAnswerEditText;
    private Button signUpButton;
    private TextView loginLinkTextView;

    private List<String> securityQuestionsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        usernameEditText = findViewById(R.id.editTextSignUpUsername);
        emailEditText = findViewById(R.id.editTextSignUpEmail);
        passwordEditText = findViewById(R.id.editTextSignUpPassword);
        confirmPasswordEditText = findViewById(R.id.editTextSignUpConfirmPassword);
        securityQuestionSpinner = findViewById(R.id.spinnerSecurityQuestions);
        securityAnswerEditText = findViewById(R.id.editTextSecurityAnswer);
        signUpButton = findViewById(R.id.buttonSignUp);
        loginLinkTextView = findViewById(R.id.textViewLoginLink);

        setupSecurityQuestionSpinner();

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processSignUp();
            }
        });

        loginLinkTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupSecurityQuestionSpinner() {
        securityQuestionsList = new ArrayList<>();
        securityQuestionsList.add("What was your childhood nickname?");
        securityQuestionsList.add("In what city were you born?");
        securityQuestionsList.add("What is the name of your first pet?");
        securityQuestionsList.add("What is your mother's maiden name?");
        securityQuestionsList.add("What was the model of your first car?");
        securityQuestionsList.add("What is the name of your favorite teacher?");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                securityQuestionsList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        securityQuestionSpinner.setAdapter(adapter);
    }

    private void processSignUp() {
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        String selectedQuestion = "";
        if (securityQuestionSpinner.getSelectedItem() != null) {
            selectedQuestion = securityQuestionSpinner.getSelectedItem().toString();
        }

        String answer = securityAnswerEditText.getText().toString().trim();

        boolean isValid = true;
        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Username is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email address");
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            isValid = false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordEditText.setError("Confirm password is required");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            isValid = false;
        }

        if (selectedQuestion.isEmpty() || (securityQuestionsList.size() > 0 && selectedQuestion.equals(securityQuestionsList.get(0)) && securityQuestionSpinner.getSelectedItemPosition() == 0 && securityQuestionsList.size() > 1) ) {
            Toast.makeText(this, "Please select a security question", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (TextUtils.isEmpty(answer)) {
            securityAnswerEditText.setError("Security answer is required");
            isValid = false;
        }

        if (!isValid) {
            Toast.makeText(SignUpActivity.this, "Please correct the errors", Toast.LENGTH_SHORT).show();
            return;
        }

        String primaryIdentifierForLogin = username;

        if (UserCredentialManager.userExists(this, primaryIdentifierForLogin)) {
            usernameEditText.setError("Username already exists");
            Toast.makeText(SignUpActivity.this, "This username is already taken.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (UserCredentialManager.signUpUser(this, primaryIdentifierForLogin, password, email, selectedQuestion, answer)) {

            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("userName", username);
            editor.putString("userEmail", email);
            editor.apply();

            Toast.makeText(SignUpActivity.this, "Sign up successful! Please log in.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(SignUpActivity.this, "Sign up failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}