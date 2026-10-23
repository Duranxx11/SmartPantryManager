package com.durangoindsamy.smartpantrymanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;

/**
 * Settings screen. Lets the user turn the daily expiry alerts on or off.
 * The choice is saved in SharedPreferences so it survives app restarts.
 */
public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "pantry_settings";
    public static final String KEY_EXPIRY_ALERTS = "expiry_alerts_enabled";

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        MaterialSwitch switchExpiryAlerts =
                findViewById(R.id.switchExpiryAlerts);
        MaterialButton btnBack = findViewById(R.id.btnSettingsBack);

        // Show the saved preference. Alerts are on by default.
        switchExpiryAlerts.setChecked(
                preferences.getBoolean(KEY_EXPIRY_ALERTS, true)
        );

        // Save the user's choice and update the daily schedule.
        switchExpiryAlerts.setOnCheckedChangeListener(
                (buttonView, isChecked) -> onExpiryAlertsChanged(isChecked)
        );

        btnBack.setOnClickListener(v -> finish());
    }

    private void onExpiryAlertsChanged(boolean enabled) {
        preferences.edit()
                .putBoolean(KEY_EXPIRY_ALERTS, enabled)
                .apply();

        if (enabled) {
            ExpiryNotificationScheduler.schedule(this);
        } else {
            ExpiryNotificationScheduler.cancel(this);
        }

        Toast.makeText(
                this,
                enabled
                        ? R.string.settings_alerts_enabled
                        : R.string.settings_alerts_disabled,
                Toast.LENGTH_SHORT
        ).show();
    }
}
