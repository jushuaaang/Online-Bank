package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView; // Import TextView if you need to set text programmatically

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.myapplication.databinding.ActivityAboutUsBinding;
// If you are using ViewBinding (recommended)


public class AboutUsActivity extends AppCompatActivity {

     private ActivityAboutUsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityAboutUsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //
        // If not using ViewBinding (as in your current file):
        setContentView(R.layout.activity_about_us);

        // This is for handling edge-to-edge display, keep it.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Example: Setting App Version Programmatically (Optional)
        TextView appVersionText = findViewById(R.id.app_version_text);
        if (appVersionText != null) {
            try {
                String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                appVersionText.setText("Version: " + versionName);
            } catch (Exception e) {
                e.printStackTrace();
                appVersionText.setText("Version: N/A");
            }
        }

        // TextView dev1Name = findViewById(R.id.dev1_name);
        // dev1Name.setText("Actual Developer Name 1");
        //
        // TextView appInfo = findViewById(R.id.app_info_text);
        // appInfo.setText("Detailed information about what my amazing app does...");
    }
}