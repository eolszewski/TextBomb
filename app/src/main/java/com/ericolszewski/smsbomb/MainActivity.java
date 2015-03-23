package com.ericolszewski.smsbomb;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
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
    private static final int REQUEST_CONTACTPICKER = 1, SEND_ANONYMOUS_TEXT = 2;
    Button sendSMSButton, browseContactsButton, setIntervalButton, anonymousTextButton;
    EditText phoneNumberEditText, messageEditText, quantityEditText, intervalEditText;
    Context context;
    String defaultSmsApp;
    //endregion

    //region View Creation
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendSMSButton = (Button) findViewById(R.id.buttonSendMessages);
        browseContactsButton = (Button) findViewById(R.id.buttonBrowse);
        anonymousTextButton = (Button) findViewById(R.id.buttonAnonymousText);
        setIntervalButton = (Button) findViewById(R.id.buttonInterval);
        phoneNumberEditText = (EditText) findViewById(R.id.editTextPhoneNumber);
        messageEditText = (EditText) findViewById(R.id.editTextMessage);
        quantityEditText = (EditText) findViewById(R.id.editTextNumber);
        intervalEditText = (EditText) findViewById(R.id.editTextInterval);

        context = this;

        defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(context);

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

        anonymousTextButton.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            public void onClick(View v) {

                final String myPackageName = getPackageName();
                if (!Telephony.Sms.getDefaultSmsPackage(context).equals(myPackageName)) {

                    //Change the default sms app to my app
                    Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.getPackageName());
                    startActivityForResult(intent, SEND_ANONYMOUS_TEXT);
                } else {
                    WriteSms("hey", "7138258982");
                }
            }
        });
    }
    //endregion

    //region Overwritten Methods
    @TargetApi(Build.VERSION_CODES.KITKAT)
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
        if (requestCode == SEND_ANONYMOUS_TEXT)
        {
            if(resultCode == RESULT_OK)
            {
                final String myPackageName = getPackageName();
                if (Telephony.Sms.getDefaultSmsPackage(context).equals(myPackageName)) {

                    //Write to the default sms app
                    WriteSms("hey", "7133646652");
                }
            }
        }
    }

    //Write to the default sms app
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void WriteSms(String message, String phoneNumber) {

        phoneNumber = phoneNumberEditText.getText().toString();
        String optionalPlus = phoneNumber.substring(0, 1);
        String sanitizedPhoneNumber = phoneNumber.replaceAll("[^\\d.]", "");
        if (optionalPlus.equals("+")) {
            sanitizedPhoneNumber = String.format("+%s", sanitizedPhoneNumber);
        }

        //Put content values
        ContentValues values = new ContentValues();
        values.put(Telephony.Sms.ADDRESS, phoneNumber);
        values.put(Telephony.Sms.DATE, System.currentTimeMillis());
        values.put(Telephony.Sms.BODY, phoneNumberEditText.getText().toString());
        values.put(Telephony.Sms.CREATOR, sanitizedPhoneNumber);

        //Insert the message
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            context.getContentResolver().insert(Telephony.Sms.Sent.CONTENT_URI, values);
        }
        else {
            context.getContentResolver().insert(Uri.parse("content://sms/sent"), values);
        }

        //Change my sms app to the last default sms
        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, defaultSmsApp);
        context.startActivity(intent);
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
            if (timeComponents.length > 1) {
                interval += Integer.parseInt(timeComponents[0]) * 60000;
                interval += Integer.parseInt(timeComponents[2]) * 1000;
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

        public CustomTimePickerDialog(Context context, OnTimeSetListener callBack, int minute, int second, boolean is24HourView) {
            super(context, callBack, minute, second, is24HourView);
        }

        @Override
        public void onTimeChanged(TimePicker timePicker, int minute, int second) {
            super.onTimeChanged(timePicker, minute, second);
            super.setTitle(String.format("%d minutes and %d seconds", minute, second));
        }
    }

    private CustomTimePickerDialog.OnTimeSetListener timeSetListener = new CustomTimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int minute, int second) {
            intervalEditText.setText(String.format("%d mins %d secs", minute, second), TextView.BufferType.EDITABLE);
        }
    };
    //endregion

    private class SendMessagesTask extends AsyncTask<String, Integer, Long> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(MainActivity.this, "Wait", "Texts are being sent to your cell phone provider's servers.");
        }

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

        protected void onPostExecute(Long result) {
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Texts are being sent.",
                    Toast.LENGTH_LONG).show();
        }
    }
}