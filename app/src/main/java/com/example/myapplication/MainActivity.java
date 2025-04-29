package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNavView);
        setupBottomNavigation();

        //para home fragment lagi simula
        openFragment(new HomeFragment());
    }

    @SuppressLint("NonConstantResourceId")
    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    openFragment(new HomeFragment());
                    return true;
                case R.id.nav_transactions:
                    openFragment(new TransactionsFragment());
                    return true;
                case R.id.nav_settings:
                    openFragment(new SettingsFragment());
                    return true;
                default:
                    return false;
            }
        });
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout, fragment);
        transaction.commit();
    }
}
