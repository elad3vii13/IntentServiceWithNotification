/*
 * Copyright (C) 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.hamama.application;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.ToggleButton;


/**
 * MainActivity for the Stand up! app. Contains a toggle button that
 * sets an alarm which delivers a Stand up notification every 15 minutes.
 */
public class ApplicationSettings extends AppCompatActivity {
    Spinner spinner;
    ToggleButton alarmToggle;
    long interval = AlarmManager.INTERVAL_DAY;
    // Notification ID.
    private static final int NOTIFICATION_ID = 0;
    // Notification channel IfD.
    private static final String PRIMARY_CHANNEL_ID =
            "primary_notification_channel";
    private NotificationManager mNotificationManager;


    /**
     * Initializes the activity.
     *
     * @param savedInstanceState The current state data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_settings);

        spinner = findViewById(R.id.spinner);

        String[] items = new String[]{ "15 minutes", "30 minutes", "1 hour", "24 hours"};
        final long[] items_value = new long[]{ AlarmManager.INTERVAL_FIFTEEN_MINUTES, AlarmManager.INTERVAL_HALF_HOUR, AlarmManager.INTERVAL_HOUR, AlarmManager.INTERVAL_HALF_DAY};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        spinner.setAdapter(adapter);
        //spinner.setSelection(positionDefault);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
               interval = items_value[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mNotificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

        alarmToggle = findViewById(R.id.alarmToggle);

        // Set up the Notification Broadcast Intent.
        Intent notifyIntent = new Intent(this, AlarmReceiver.class);

        boolean alarmUp = (PendingIntent.getBroadcast(this, NOTIFICATION_ID,
                notifyIntent, PendingIntent.FLAG_NO_CREATE) != null);
        alarmToggle.setChecked(alarmUp);

        final PendingIntent notifyPendingIntent = PendingIntent.getBroadcast
                (this, NOTIFICATION_ID, notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        final AlarmManager alarmManager = (AlarmManager) getSystemService
                (ALARM_SERVICE);

        // Set the click listener for the toggle button.
        alarmToggle.setOnCheckedChangeListener
                (new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged
                            (CompoundButton buttonView, boolean isChecked) {
                        String toastMessage;
                        if (isChecked) {

                            long repeatInterval = 1;

                            long triggerTime = SystemClock.elapsedRealtime()
                                    + repeatInterval;

                            // If the Toggle is turned on, set the repeating alarm with
                            // a 15 minute interval.
//                            if (alarmManager != null) {
//                                alarmManager.setRepeating
//                                        (AlarmManager.ELAPSED_REALTIME_WAKEUP,
//                                                triggerTime, repeatInterval,
//                                                notifyPendingIntent);
//                            }

                           if (alarmManager != null) {
                               alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, notifyPendingIntent);
                               spinner.setEnabled(false);
                           }

                            // Set the toast message for the "on" case.
                            toastMessage = getString(R.string.alarm_on_toast);

                        } else {
                            // Cancel notification if the alarm is turned off.
                            mNotificationManager.cancelAll();

                            if (alarmManager != null) {
                                alarmManager.cancel(notifyPendingIntent);

                            }
                            // Set the toast message for the "off" case.
                            toastMessage = getString(R.string.alarm_off_toast);
                            spinner.setEnabled(true);
                        }

                        // Show a toast to say the alarm is turned on or off.
//                        Toast.makeText(MainActivity.this, toastMessage,
//                                Toast.LENGTH_SHORT).show();
                    }
                });

        // Create the notification channel.
        createNotificationChannel();
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences sh = getSharedPreferences("Shared", MODE_PRIVATE);
        SharedPreferences.Editor editor = sh.edit();
        editor.putInt("position", spinner.getSelectedItemPosition());
        editor.putBoolean("checked", alarmToggle.isChecked());
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        alarmToggle.isChecked();
        SharedPreferences sh1 = getSharedPreferences("Shared", MODE_PRIVATE);
        int p = sh1.getInt("position", 0);
        boolean b = sh1.getBoolean("checked", false);
        spinner.setSelection(p);
        alarmToggle.setChecked(b);
    }

    /**
     * Creates a Notification channel, for OREO and higher.
     */
    public void createNotificationChannel() {

        // Create a notification manager object.
        mNotificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Notification channels are only available in OREO and higher.
        // So, add a check on SDK version.
        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.O) {

            // Create the NotificationChannel with all the parameters.
            NotificationChannel notificationChannel = new NotificationChannel
                    (PRIMARY_CHANNEL_ID,
                            "Stand up notification",
                            NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Notifies every 15 minutes to " +
                    "stand up and walk");
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }
}