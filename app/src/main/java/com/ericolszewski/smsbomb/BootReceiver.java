package com.ericolszewski.smsbomb;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.Calendar;

import classes.Message;
import services.MessageService;
import utilities.Utilities;

/**
 * Created by ericolszewski on 5/31/15.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            ArrayList<Message> messages = new ArrayList<Message>();

            Intent messageIntent = new Intent(context, MessageService.class);
            AlarmManager alarmManager = (AlarmManager)context.getSystemService(context.ALARM_SERVICE);

            DatabaseAdapter databaseAdapter;
            databaseAdapter = new DatabaseAdapter(context);
            databaseAdapter.open();
            Cursor cursor  =  databaseAdapter.getAllRows();

            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(0);
                    String date = cursor.getString(2);
                    String frequency = cursor.getString(5);

                    messageIntent.putExtra("id", Integer.toString(id));
                    PendingIntent pendingIntent = PendingIntent.getService(context, id, messageIntent, 0);
                    try {
                        alarmManager.cancel(pendingIntent);
                    } catch (Exception e) {

                    }

                    Calendar calendar = Utilities.formattedTimeToCalendar(date);
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() - Utilities.frequencyToMilliseconds(frequency), Utilities.frequencyToMilliseconds(frequency), pendingIntent);
                } while(cursor.moveToNext());
            }
            cursor.close();
        }
    }
}