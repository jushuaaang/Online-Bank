package com.example.myapplication; // Your package name

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem; // For handling up navigation
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull; // For onSupportNavigateUp and onRequestPermissionsResult if not using launchers
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

public class ProfileActivity extends AppCompatActivity {

    private ImageView detailedProfileImageView;
    private EditText nameEditText;
    private EditText emailEditText;
    // Add EditTexts for other profile fields as needed (e.g., phone, bio)
    private Button saveProfileButton;
    private Toolbar profileToolbar;

    private Uri selectedImageUri; // To store the URI of the image chosen by the user

    // ActivityResultLauncher for picking an image from the gallery
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        selectedImageUri = imageUri; // Store the selected URI
                        // Use Glide (or Picasso) to load the image efficiently
                        Glide.with(this)
                                .load(selectedImageUri)
                                .circleCrop() // Optional: if you want a circular image
                                .placeholder(R.drawable.ic_profile_placeholder) // Your placeholder
                                .into(detailedProfileImageView);
                    }
                }
            });

    // ActivityResultLauncher for requesting permissions
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openGallery(); // Permission granted, open gallery
                } else {
                    // Permission denied
                    Toast.makeText(this, "Permission denied to access gallery.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ensure you have an activity_profile.xml layout file
        setContentView(R.layout.activity_profile);

        profileToolbar = findViewById(R.id.profileToolbar); // Assuming you have a Toolbar with this ID
        setSupportActionBar(profileToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show back button
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Edit Profile"); // Or get string resource
        }

        detailedProfileImageView = findViewById(R.id.detailedProfileImageView); // ImageView in your layout
        nameEditText = findViewById(R.id.nameEditText);               // EditText for name
        emailEditText = findViewById(R.id.emailEditText);             // EditText for email
        saveProfileButton = findViewById(R.id.saveProfileButton);       // Button to save changes

        loadUserData(); // Load existing user data (name, email, and profile image)

        detailedProfileImageView.setOnClickListener(v -> checkPermissionAndOpenGallery());

        saveProfileButton.setOnClickListener(v -> saveUserData());
    }

    @SuppressLint("NewApi")
    private void checkPermissionAndOpenGallery() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 (API 33) and above
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else { // Below Android 13
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openGallery(); // Permission already granted
        } else if (shouldShowRequestPermissionRationale(permission)) {
            // Show an explanation to the user *asynchronously*
            // This is a good place to show a dialog explaining why you need the permission
            Toast.makeText(this, "Gallery access is needed to select a profile picture.", Toast.LENGTH_LONG).show();
            requestPermissionLauncher.launch(permission); // Request permission after explanation
        } else {
            // No explanation needed; request the permission directly
            requestPermissionLauncher.launch(permission);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void loadUserData() {
        SharedPreferences sharedPrefs = getSharedPreferences("UserProfilePrefs", MODE_PRIVATE);

        // Load name and email - Replace with your actual default values or logic
        nameEditText.setText(sharedPrefs.getString("userName", "Enter Name"));
        emailEditText.setText(sharedPrefs.getString("userEmail", "Enter Email"));

        // Load saved image URI
        String savedImageUriString = sharedPrefs.getString("profileImageUri", null);
        if (savedImageUriString != null) {
            selectedImageUri = Uri.parse(savedImageUriString); // Store it for saving later
            Glide.with(this)
                    .load(selectedImageUri)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(detailedProfileImageView);
        } else {
            // Load default placeholder if no image is saved
            Glide.with(this)
                    .load(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(detailedProfileImageView);
        }
    }

    private void saveUserData() {
        String newName = nameEditText.getText().toString().trim();
        String newEmail = emailEditText.getText().toString().trim();

        // Basic validation (optional, but recommended)
        if (newName.isEmpty()) {
            nameEditText.setError("Name cannot be empty");
            nameEditText.requestFocus();
            return;
        }
        if (newEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            emailEditText.setError("Enter a valid email");
            emailEditText.requestFocus();
            return;
        }

        SharedPreferences sharedPrefs = getSharedPreferences("UserProfilePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        editor.putString("userName", newName);
        editor.putString("userEmail", newEmail);

        if (selectedImageUri != null) {
            // Persist the selectedImageUri (as a String)
            editor.putString("profileImageUri", selectedImageUri.toString());
        } else {
            // If user wants to remove image, you might clear it or save a specific "no_image" marker
            // For now, if no new image is selected, we keep the old one or nothing if it was null.
            // If you want to allow REMOVING the image, you'd need an explicit "remove" button
            // and then you could do editor.remove("profileImageUri");
        }

        editor.apply(); // Save changes

        Toast.makeText(this, "Profile Saved", Toast.LENGTH_SHORT).show();

        // Indicate that data has changed so SettingsFragment (or any calling activity) can refresh
        setResult(Activity.RESULT_OK);
        finish(); // Close ProfileActivity and return to the previous screen
    }

    // Handle the back button in the Toolbar (Up navigation)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // This ID refers to the Home Up button
            // You can either finish the activity or use NavUtils/NavDeepLinkBuilder
            // For simple cases, onBackPressed() or finish() is fine.
            // Using getOnBackPressedDispatcher().onBackPressed() is the recommended way now.
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
     public void onBackPressed() {
         // Handle custom back press behavior if needed
         super.onBackPressed();
     }
}