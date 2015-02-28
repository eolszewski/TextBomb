package com.ericolszewski.smsbomb;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract;
import android.app.TimePickerDialog;
import android.widget.TimePicker;
import android.content.Context;
import android.telephony.SmsManager;
import android.os.SystemClock;
import android.os.AsyncTask;

public class MainActivity extends Activity {

    //region Class Variables
    private static final int REQUEST_CONTACTPICKER = 1;
    Button sendSMSButton, browseContactsButton, setIntervalButton;
    EditText phoneNumberEditText, messageEditText, quantityEditText, intervalEditText;
    //endregion

    //region View Creation
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendSMSButton = (Button) findViewById(R.id.buttonSendMessages);
        browseContactsButton = (Button) findViewById(R.id.buttonBrowse);
        setIntervalButton = (Button) findViewById(R.id.buttonInterval);
        phoneNumberEditText = (EditText) findViewById(R.id.editTextPhoneNumber);
        messageEditText = (EditText) findViewById(R.id.editTextMessage);
        quantityEditText = (EditText) findViewById(R.id.editTextNumber);
        intervalEditText = (EditText) findViewById(R.id.editTextInterval);

        sendSMSButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                sendSMSMessage();
            }
        });
        browseContactsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                selectContact();
            }
        });
        setIntervalButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                CustomTimePickerDialog timePickerDialog = new CustomTimePickerDialog(MainActivity.this, timeSetListener, 0, 0, true);
                timePickerDialog.setTitle("Set minutes and seconds");
                timePickerDialog.show();
            }
        });
    }
    //endregion

    //region Overwritten Methods
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CONTACTPICKER)
        {
            if(resultCode == RESULT_OK)
            {
                Uri contentUri = data.getData();
                String contactId = contentUri.getLastPathSegment();
                Cursor cursor = getContentResolver().query(
                        Phone.CONTENT_URI, null,
                        Phone._ID + "=?",
                        new String[]{contactId}, null);
                Boolean numbersExist = cursor.moveToFirst();
                int phoneNumberColumnIndex = cursor.getColumnIndex(Phone.NUMBER);
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    //endregion

    //region View Methods
    // Method for launching contact picker
    private void selectContact()
    {
        Intent intent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, REQUEST_CONTACTPICKER);
    }

    // Method for sending SMS Messages
    protected void sendSMSMessage() {
        String phoneNumber, sanitizedPhoneNumber, optionalPlus, message;
        int number, error = 0, interval = 0;
        String[] timeComponents;

        try {
            phoneNumber = phoneNumberEditText.getText().toString();
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

            number = Integer.parseInt(quantityEditText.getText().toString());
            error++;

            timeComponents = intervalEditText.getText().toString().split("\\s+");
            if (timeComponents.length > 0) {
                interval += Integer.parseInt(timeComponents[0]) * 3600000;
                interval += Integer.parseInt(timeComponents[2]) * 60000;
            }

            if (number <= 0) {
                Toast.makeText(getApplicationContext(), "Please input a number greater than 0.",
                        Toast.LENGTH_LONG).show();
            } else {
                new SendMessagesTask().execute(sanitizedPhoneNumber, Integer.toString(number),
                        message, Integer.toString(interval));
            }
        } catch (Exception e) {
            if (error == 0) {
                Toast.makeText(getApplicationContext(), "Please input a valid phone number.",
                        Toast.LENGTH_LONG).show();
            } else if (error == 1) {
                Toast.makeText(getApplicationContext(), "Please input a message to send.",
                        Toast.LENGTH_LONG).show();
            } else if (error == 2) {
                Toast.makeText(getApplicationContext(), "Please input a number of times to send your message.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
    //endregion

    //region Custom TimePicker
    public static class CustomTimePickerDialog extends TimePickerDialog{

        public CustomTimePickerDialog(Context context, OnTimeSetListener callBack, int hourOfDay, int minute, boolean is24HourView) {
            super(context, callBack, hourOfDay, minute, is24HourView);
        }

        @Override
        public void onTimeChanged(TimePicker timePicker, int hourOfDay, int minute) {
            super.onTimeChanged(timePicker, hourOfDay, minute);
            super.setTitle(String.format("%d hours and %d minutes", hourOfDay, minute));
        }
    }

    private CustomTimePickerDialog.OnTimeSetListener timeSetListener = new CustomTimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            intervalEditText.setText(String.format("%d hours %d mins", hourOfDay, minute), TextView.BufferType.EDITABLE);
        }
    };
    //endregion

    private class SendMessagesTask extends AsyncTask<String, Integer, Long> {
        @Override
        protected Long doInBackground(String... params) {
            for(int count = 0; count < Integer.parseInt(params[1]); count++){
                try {
                    SystemClock.sleep(Integer.parseInt(params[3]));
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(params[0], null, params[2], null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        protected void onPreExecute() {
            Toast.makeText(getApplicationContext(), "Texts are currently being sent. " +
                            "Do not close the app while it is running in the background" +
                            " or all of your messages may not send.",
                    Toast.LENGTH_LONG).show();
        }

        protected void onPostExecute(Long result) {
            Toast.makeText(getApplicationContext(), "Texts sent.",
                    Toast.LENGTH_LONG).show();
        }
    }
}