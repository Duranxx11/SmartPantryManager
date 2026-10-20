package com.durangoindsamy.smartpantrymanager;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.graphics.Color;
import android.widget.Toast;

import androidx.work.WorkManager;

public class SettingsActivity extends Activity {

    public static final String PREFS_NAME = "pantry_settings";
    public static final String KEY_EXPIRY_ALERTS = "expiry_alerts_enabled";

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences(
                PREFS_NAME,
                MODE_PRIVATE
        );

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        layout.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("Settings");
        title.setTextSize(26);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 32);
        layout.addView(title);

        TextView description = new TextView(this);
        description.setText(
                "Manage your Smart Pantry Manager preferences."
        );
        description.setTextSize(16);
        description.setTextColor(Color.DKGRAY);
        description.setPadding(0, 0, 0, 24);
        layout.addView(description);

        Switch expirySwitch = new Switch(this);
        expirySwitch.setText("Expiry alerts enabled");
        expirySwitch.setTextSize(16);

        // Load the saved preference. Default is enabled.
        boolean alertsEnabled = preferences.getBoolean(
                KEY_EXPIRY_ALERTS,
                true
        );

        expirySwitch.setChecked(alertsEnabled);
        layout.addView(expirySwitch);

        TextView note = new TextView(this);
        note.setText(
                "Receive reminders about ingredients that have expired " +
                        "or are expiring soon."
        );
        note.setTextSize(14);
        note.setTextColor(Color.DKGRAY);
        note.setPadding(0, 16, 0, 32);
        layout.addView(note);

        // Save the user's choice and update the daily schedule.
        expirySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            preferences.edit()
                    .putBoolean(KEY_EXPIRY_ALERTS, isChecked)
                    .apply();

            if (isChecked) {
                ExpiryNotificationScheduler.schedule(this);

                Toast.makeText(
                        this,
                        "Expiry alert schedule enabled",
                        Toast.LENGTH_SHORT
                ).show();

            } else {
                WorkManager.getInstance(this).cancelUniqueWork(
                        "pantry_expiry_daily_check"
                );

                Toast.makeText(
                        this,
                        "Expiry alert schedule disabled",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        Button backButton = new Button(this);
        backButton.setText("Back");
        backButton.setOnClickListener(v -> finish());
        layout.addView(backButton);

        setContentView(layout);
    }
}