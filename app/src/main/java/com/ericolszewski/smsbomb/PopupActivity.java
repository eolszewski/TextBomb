package com.ericolszewski.smsbomb;

import android.app.Activity;
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

import fragments.ScheduleTextFragment;

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
        String phoneNumber, sanitizedPhoneNumber, optionalPlus, message, date;
        int error = 0, interval = 0;
        String[] timeComponents;

        try {
            phoneNumber = recipientsEditText.getText().toString();
            optionalPlus = phoneNumber.substring(0, 1);
            sanitizedPhoneNumber = phoneNumber.replaceAll("[^\\d.]", "");
            if (optionalPlus.equals("+")) {
                sanitizedPhoneNumber = String.format("+%s", sanitizedPhoneNumber);
            }
            error++;

            message = messageEditText.getText().toString();
            if (message.length() == 0) {
                throw new Exception("Needs to be a message here");
            }
            error++;

            date = setFirstOccurrenceButton.getText().toString();
            error++;

            long id = databaseAdapter.insertRow(message, date, sanitizedPhoneNumber, frequencySpinner.getSelectedItem().toString());
            Toast.makeText(this, "Your message has been added.",
                    Toast.LENGTH_LONG).show();
            ScheduleTextFragment.getInstance(2);
            finish();

        } catch (Exception e) {
            if (error == 0) {
                Toast.makeText(this, "Please input a valid phone number.",
                        Toast.LENGTH_LONG).show();
            } else if (error == 1) {
                Toast.makeText(this, "Please input a message to send.",
                        Toast.LENGTH_LONG).show();
            } else if (error == 2) {
                Toast.makeText(this, "Please set a time to send your first message.",
                        Toast.LENGTH_LONG).show();
            }
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
