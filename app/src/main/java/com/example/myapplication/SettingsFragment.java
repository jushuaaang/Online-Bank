package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

public class SettingsFragment extends Fragment {

    private ImageView profileImageView;
    private TextView nameTextView;
    private TextView emailTextView;
    private Button profileButton;
    private Button logoutButton;
    private Button aboutUsButton;
    private Toolbar toolbar;

    private final ActivityResultLauncher<Intent> profileActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loadProfileDisplayData();
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Profile data refreshed", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    public SettingsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        toolbar = view.findViewById(R.id.toolbar);
        profileImageView = view.findViewById(R.id.profileImageView);
        nameTextView = view.findViewById(R.id.nameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        profileButton = view.findViewById(R.id.profileButton);
        logoutButton = view.findViewById(R.id.logoutButton);
        aboutUsButton = view.findViewById(R.id.aboutUsButton);

        loadProfileDisplayData();

        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            profileActivityResultLauncher.launch(intent);
        });

        logoutButton.setOnClickListener(v -> {
            Context context = getContext();
            if (context == null) {
                return;
            }

            Toast.makeText(context, "Logging Out...", Toast.LENGTH_SHORT).show();

            UserCredentialManager.logoutUser(context);

            // Clearing UserProfilePrefs specifically for the image URI
            // if UserCredentialManager.logoutUser() doesn't handle this specific preference file
            // or if you want to be certain it's cleared here.
            SharedPreferences profileImagePrefs = requireActivity().getSharedPreferences("UserProfilePrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor profileImageEditor = profileImagePrefs.edit();
            profileImageEditor.remove("profileImageUri");
            profileImageEditor.apply();

            Intent loginIntent = new Intent(getActivity(), LogInActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);

            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        aboutUsButton.setOnClickListener(v -> {
            if (getContext() != null) {
                Intent aboutUsIntent = new Intent(getActivity(), AboutUsActivity.class);
                startActivity(aboutUsIntent);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileDisplayData();
    }

    private void loadProfileDisplayData() {
        Context context = getContext();
        if (context == null) {
            return;
        }

        SharedPreferences userPrefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String name = userPrefs.getString("userName", "Your Name");
        String email = userPrefs.getString("userEmail", "your.email@example.com");

        nameTextView.setText(name);
        emailTextView.setText(email);

        SharedPreferences profileImagePrefs = requireActivity().getSharedPreferences("UserProfilePrefs", Context.MODE_PRIVATE);
        String imageUriString = profileImagePrefs.getString("profileImageUri", null);

        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);
            Glide.with(this)
                    .load(imageUri)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(profileImageView);
        } else {
            Glide.with(this)
                    .load(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(profileImageView);
        }
    }
}