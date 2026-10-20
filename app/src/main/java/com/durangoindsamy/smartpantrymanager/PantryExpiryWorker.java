package com.durangoindsamy.smartpantrymanager;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.durangoindsamy.smartpantrymanager.data.DatabaseHelper;
import com.durangoindsamy.smartpantrymanager.model.PantryItem;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PantryExpiryWorker extends Worker {

    private static final String CHANNEL_ID = "pantry_expiry_channel";

    public PantryExpiryWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        SharedPreferences preferences = context.getSharedPreferences(
                SettingsActivity.PREFS_NAME,
                Context.MODE_PRIVATE
        );

        boolean alertsEnabled = preferences.getBoolean(
                SettingsActivity.KEY_EXPIRY_ALERTS,
                true
        );

        if (!alertsEnabled) {
            return Result.success();
        }

        if (Build.VERSION.SDK_INT >= 33
                && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED) {
            return Result.success();
        }

        createNotificationChannel(context);

        DatabaseHelper db = DatabaseHelper.getInstance(context);
        List<PantryItem> items = db.getAllPantryItems();

        Calendar today = Calendar.getInstance();
        setStartOfDay(today);

        for (PantryItem item : items) {
            Long expiryDate = item.getExpiryDate();

            if (expiryDate == null) {
                continue;
            }

            Calendar expiry = Calendar.getInstance();
            expiry.setTimeInMillis(expiryDate);
            setStartOfDay(expiry);

            long difference = expiry.getTimeInMillis()
                    - today.getTimeInMillis();

            long daysLeft = TimeUnit.MILLISECONDS.toDays(difference);

            String message = null;

            if (daysLeft < 0) {
                message = item.getName() + " has expired.";
            } else if (daysLeft == 0) {
                message = item.getName() + " expires today.";
            } else if (daysLeft <= 3) {
                message = item.getName()
                        + " expires in " + daysLeft + " day(s).";
            }

            if (message != null) {
                // Open the pantry list with this specific ingredient selected.
                Intent intent = new Intent(
                        context,
                        PantryListActivity.class
                );

                intent.putExtra(
                        PantryListActivity.EXTRA_EXPIRY_ITEM_ID,
                        item.getId()
                );

                intent.putExtra(
                        PantryListActivity.EXTRA_EXPIRY_ITEM_NAME,
                        item.getName()
                );

                intent.setAction(
                        "com.durangoindsamy.smartpantrymanager.EXPIRY_"
                                + item.getId()
                );

                intent.addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                );

                int pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    pendingIntentFlags |= PendingIntent.FLAG_IMMUTABLE;
                }

                PendingIntent contentIntent = PendingIntent.getActivity(
                        context,
                        (int) (item.getId() & 0x7FFFFFFF),
                        intent,
                        pendingIntentFlags
                );

                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(context, CHANNEL_ID)
                                .setSmallIcon(
                                        android.R.drawable.ic_dialog_info
                                )
                                .setContentTitle("Pantry Expiry Alert")
                                .setContentText(message)
                                .setStyle(
                                        new NotificationCompat.BigTextStyle()
                                                .bigText(message)
                                )
                                .setPriority(
                                        NotificationCompat.PRIORITY_DEFAULT
                                )
                                .setContentIntent(contentIntent)
                                .setAutoCancel(true);

                try {
                    // Use a stable ID so the same ingredient's alert
                    // updates rather than creating duplicates each run.
                    int notificationId =
                            (int) (item.getId() & 0x7FFFFFFF);

                    NotificationManagerCompat.from(context)
                            .notify(notificationId, builder.build());

                } catch (SecurityException ignored) {
                    // Notifications may have been disabled by the user.
                }
            }
        }

        return Result.success();
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Pantry Expiry Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            channel.setDescription(
                    "Alerts for expired and soon-to-expire pantry ingredients"
            );

            NotificationManager manager =
                    context.getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void setStartOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
}