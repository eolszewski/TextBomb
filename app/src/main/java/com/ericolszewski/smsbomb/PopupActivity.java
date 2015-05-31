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

import com.android.datetimepicker.date.DatePickerDialog;
import com.android.datetimepicker.time.RadialPickerLayout;
import com.android.datetimepicker.time.TimePickerDialog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by ericolszewski on 5/30/15.
 */
public class PopupActivity extends Activity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, View.OnClickListener {

    //region Class Variables
    private static final int REQUEST_CONTACTPICKER = 1;
    private static final String TIME_PATTERN = "HH:mm";

    private Button scheduleSMSButton, browseContactsButton, setFirstOccurrenceButton;
    private EditText phoneNumberEditText, messageEditText;
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
        phoneNumberEditText = (EditText) findViewById(R.id.editTextPhoneNumber);
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

    // Launch Contact Picker
    private void selectContact()
    {
        Intent intent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, REQUEST_CONTACTPICKER);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonSetFirstOccurrence:
                DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show(this.getFragmentManager(), "datePicker");
                break;
            case R.id.buttonAddText:
                break;
            case R.id.buttonBrowse:
                selectContact();
                break;
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
                phoneNumberEditText.setText(phoneNumber, TextView.BufferType.EDITABLE);
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
