package services;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;

import com.ericolszewski.smsbomb.DatabaseAdapter;

import java.util.Calendar;

import utilities.Utilities;

/**
 * Created by ericolszewski on 5/31/15.
 */
public class MessageService extends Service {

    private DatabaseAdapter databaseAdapter;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        databaseAdapter = new DatabaseAdapter(this);
        databaseAdapter.open();
        Bundle b = intent.getExtras();
        String id = b.getString("id");
        Cursor cursor = databaseAdapter.getRow(Long.parseLong(id));

        String text = cursor.getString(1);
        String date = cursor.getString(2);
        String updatedAt = cursor.getString(3);
        String recipients = cursor.getString(4);
        String frequency = cursor.getString(5);

        new SendMessagesTask().execute(recipients, text);

        Calendar calendar = Utilities.formattedTimeToCalendar(updatedAt);
        calendar.add(Calendar.MILLISECOND, Utilities.frequencyToMilliseconds(frequency));

        databaseAdapter.updateRow(Long.parseLong(id), text, date, Utilities.calendarToFormattedString(calendar), recipients, frequency);

        return super.onStartCommand(intent, flags, startId);
    }

    private class SendMessagesTask extends AsyncTask<String, Integer, Long> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Long doInBackground(String... params) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(params[0], null, params[1], null, null);
            return null;
        }

        protected void onPostExecute(Long result) {
            stopSelf();
        }
    }
}
