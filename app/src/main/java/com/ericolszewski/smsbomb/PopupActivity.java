package com.ericolszewski.smsbomb;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.datetimepicker.date.DatePickerDialog;
import com.android.datetimepicker.time.RadialPickerLayout;
import com.android.datetimepicker.time.TimePickerDialog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import classes.Constants;
import fragments.ScheduleTextFragment;
import services.MessageService;
import utilities.Utilities;

/**
 * Created by ericolszewski on 5/30/15.
 */
public class PopupActivity extends Activity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, View.OnClickListener {

    //region Class Variables
    private static final int REQUEST_CONTACTPICKER = 1;
    private static final String TIME_PATTERN = "HH:mm";

    private Button scheduleSMSButton, browseContactsButton, setFirstOccurrenceButton;
    private EditText recipientsEditText, messageEditText;
    private Calendar calendar;
    private DateFormat dateFormat;
    private SimpleDateFormat timeFormat;
    private Spinner frequencySpinner;

    private DatabaseAdapter databaseAdapter;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);

        scheduleSMSButton = (Button) findViewById(R.id.buttonScheduleMessage);
        scheduleSMSButton.setOnClickListener(this);
        browseContactsButton = (Button) findViewById(R.id.buttonBrowse);
        browseContactsButton.setOnClickListener(this);
        setFirstOccurrenceButton = (Button) findViewById(R.id.buttonSetFirstOccurrence);
        setFirstOccurrenceButton.setOnClickListener(this);
        recipientsEditText = (EditText) findViewById(R.id.editTextRecipients);
        messageEditText = (EditText) findViewById(R.id.editTextMessage);
        frequencySpinner = (Spinner) findViewById(R.id.frequencySpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.frequency_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        frequencySpinner.setAdapter(adapter);

        calendar = Calendar.getInstance();
        dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        timeFormat = new SimpleDateFormat(TIME_PATTERN, Locale.getDefault());

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        getWindow().setLayout((int) (dm.widthPixels * 0.9), (int) (dm.heightPixels * 0.8));

        databaseAdapter = new DatabaseAdapter(this);
        databaseAdapter.open();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonSetFirstOccurrence:
                DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show(this.getFragmentManager(), "datePicker");
                break;
            case R.id.buttonScheduleMessage:
                scheduleMessage();
                break;
            case R.id.buttonBrowse:
                selectContact();
                break;
        }
    }

    // Launch Contact Picker
    private void selectContact()
    {
        Intent intent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, REQUEST_CONTACTPICKER);
    }

    // Add Text To Database
    private void scheduleMessage() {
        if (Constants.beta) {
            String phoneNumber, sanitizedPhoneNumber, message, date, frequency;
            Calendar calendar;

            Intent intent = new Intent(this, MessageService.class);
            AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

            phoneNumber = recipientsEditText.getText().toString();
            message = messageEditText.getText().toString();
            date = setFirstOccurrenceButton.getText().toString();
            frequency = frequencySpinner.getSelectedItem().toString();

            sanitizedPhoneNumber = Utilities.checkMessageForErrors(phoneNumber, message, date, frequency);
            if (sanitizedPhoneNumber.substring(0, 5).equals("Error")) {
                Toast.makeText(this, sanitizedPhoneNumber.substring(7),
                        Toast.LENGTH_LONG).show();
            } else {
                calendar = Utilities.formattedTimeToCalendar(date);
                calendar.add(Calendar.MILLISECOND, Utilities.frequencyToMilliseconds(frequency));

                long id = databaseAdapter.insertRow(message, date, date, sanitizedPhoneNumber, frequency);
                intent.putExtra("id", Long.toString(id));
                PendingIntent pendingIntent = PendingIntent.getService(this, (int)id, intent, 0);
                try {
                    alarmManager.cancel(pendingIntent);
                } catch (Exception e) {

                }
                Toast.makeText(this, "Your message has been added.", Toast.LENGTH_LONG).show();

                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() - Utilities.frequencyToMilliseconds(frequency), Utilities.frequencyToMilliseconds(frequency), pendingIntent);
                ScheduleTextFragment.getInstance(2);
                finish();
            }
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Coming Soon");
            alertDialog.setMessage("This feature will be available by June 14th, if you'd like to make suggestions for this feature, please email me (my information can be found in the Play Store for this app).");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }

    //region Overwritten Methods
    // Retrieve Number From Address Book
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == this.RESULT_OK)
        {
            Uri contentUri = data.getData();
            String contactId = contentUri.getLastPathSegment();
            Cursor cursor = this.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone._ID + "=?",
                    new String[]{contactId}, null);
            Boolean numbersExist = cursor.moveToFirst();
            int phoneNumberColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            String phoneNumber = "";
            while (numbersExist)
            {
                phoneNumber = cursor.getString(phoneNumberColumnIndex);
                phoneNumber = phoneNumber.trim();
                numbersExist = cursor.moveToNext();
            }
            if (!phoneNumber.equals(""))
            {
                recipientsEditText.setText(phoneNumber, TextView.BufferType.EDITABLE);
            }
        }
    }

    // Date and Time Pickers
    @Override
    public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
        calendar.set(year, monthOfYear, dayOfMonth);
        TimePickerDialog.newInstance(this, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show(this.getFragmentManager(), "timePicker");
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        setFirstOccurrenceButton.setText(dateFormat.format(calendar.getTime()) + " " + timeFormat.format(calendar.getTime()));
    }

    // Close Database
    @Override
    public void onDestroy() {
        super.onDestroy();
        databaseAdapter.close();
    }
    //endregion
}
