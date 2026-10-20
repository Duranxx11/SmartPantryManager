package com.durangoindsamy.smartpantrymanager;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class ExpiryNotificationScheduler {

    private static final String WORK_NAME =
            "pantry_expiry_daily_check";

    public static void schedule(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build();

        PeriodicWorkRequest request =
                new PeriodicWorkRequest.Builder(
                        PantryExpiryWorker.class,
                        1,
                        TimeUnit.DAYS
                )
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniquePeriodicWork(
                        WORK_NAME,
                        ExistingPeriodicWorkPolicy.KEEP,
                        request
                );
    }
}