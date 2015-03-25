package fragments;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract;

import com.android.datetimepicker.date.DatePickerDialog;
import com.android.datetimepicker.time.RadialPickerLayout;
import com.android.datetimepicker.time.TimePickerDialog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.ericolszewski.smsbomb.R;

public class FakeTextFragment extends Fragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, View.OnClickListener {

    //region Class Variables
    private static final int REQUEST_CONTACTPICKER = 1, SEND_ANONYMOUS_TEXT = 2;
    private static final String TIME_PATTERN = "HH:mm";

    private Button sendSMSButton, browseContactsButton, setDateAndTimeButton;
    private EditText phoneNumberEditText, messageEditText;
    private String defaultSmsApp;
    private Calendar calendar;
    private DateFormat dateFormat;
    private SimpleDateFormat timeFormat;
    private RadioButton inboxRadioButton, sentRadioButton, draftRadioButton,
    outboxRadioButton, failedRadioButton, queuedRadioButton;
    //endregion

    public static FakeTextFragment getInstance(int position) {
        FakeTextFragment fakeTextFragment = new FakeTextFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        fakeTextFragment.setArguments(args);
        return fakeTextFragment;
    }

    //region View Creation
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_fake_text, container, false);
        Bundle bundle = getArguments();

        if (bundle != null) {
            sendSMSButton = (Button) layout.findViewById(R.id.buttonSendMessages);
            sendSMSButton.setOnClickListener(this);
            browseContactsButton = (Button) layout.findViewById(R.id.buttonBrowse);
            browseContactsButton.setOnClickListener(this);
            setDateAndTimeButton = (Button) layout.findViewById(R.id.buttonSetDateAndTime);
            setDateAndTimeButton.setOnClickListener(this);
            phoneNumberEditText = (EditText) layout.findViewById(R.id.editTextPhoneNumber);
            messageEditText = (EditText) layout.findViewById(R.id.editTextMessage);

            inboxRadioButton = (RadioButton) layout.findViewById(R.id.radioButtonInbox);
            inboxRadioButton.setOnClickListener(this);
            sentRadioButton = (RadioButton) layout.findViewById(R.id.radioButtonSent);
            sentRadioButton.setOnClickListener(this);
            draftRadioButton = (RadioButton) layout.findViewById(R.id.radioButtonDraft);
            draftRadioButton.setOnClickListener(this);
            outboxRadioButton = (RadioButton) layout.findViewById(R.id.radioButtonOutbox);
            outboxRadioButton.setOnClickListener(this);
            failedRadioButton = (RadioButton) layout.findViewById(R.id.radioButtonFailed);
            failedRadioButton.setOnClickListener(this);
            queuedRadioButton = (RadioButton) layout.findViewById(R.id.radioButtonQueued);
            queuedRadioButton.setOnClickListener(this);

            messageEditText = (EditText) layout.findViewById(R.id.editTextMessage);

            defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(getActivity());

            calendar = Calendar.getInstance();
            dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
            timeFormat = new SimpleDateFormat(TIME_PATTERN, Locale.getDefault());
        }

        return layout;

    }
    //endregion

    //region Overwritten Methods
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CONTACTPICKER)
        {
            if(resultCode == getActivity().RESULT_OK)
            {
                Uri contentUri = data.getData();
                String contactId = contentUri.getLastPathSegment();
                Cursor cursor = getActivity().getContentResolver().query(
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
            if(resultCode == getActivity().RESULT_OK)
            {
                final String myPackageName = getActivity().getPackageName();
                if (Telephony.Sms.getDefaultSmsPackage(getActivity()).equals(myPackageName)) {
                    WriteSms(messageEditText.getText().toString(), phoneNumberEditText.getText().toString());
                }
            }
        }
    }

    // Write to the default sms app
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void WriteSms(String message, String phoneNumber) {

        ContentValues values = new ContentValues();
        values.put(Telephony.Sms.ADDRESS, phoneNumber);
        values.put(Telephony.Sms.DATE, System.currentTimeMillis());
        values.put(Telephony.Sms.BODY, message);
        values.put(Telephony.Sms.DATE, calendar.getTimeInMillis());

        //Insert the message
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (inboxRadioButton.isChecked()) {
                getActivity().getContentResolver().insert(Telephony.Sms.Inbox.CONTENT_URI, values);
            } else if (sentRadioButton.isChecked()) {
                getActivity().getContentResolver().insert(Telephony.Sms.Sent.CONTENT_URI, values);
            } else if (draftRadioButton.isChecked()) {
                getActivity().getContentResolver().insert(Telephony.Sms.Draft.CONTENT_URI, values);
            } else if (outboxRadioButton.isChecked()) {
                getActivity().getContentResolver().insert(Telephony.Sms.Outbox.CONTENT_URI, values);
            } else if (queuedRadioButton.isChecked()) {
                getActivity().getContentResolver().insert(Uri.parse("content://sms/queued"), values);
            } else if (failedRadioButton.isChecked()) {
                getActivity().getContentResolver().insert(Uri.parse("content://sms/failed"), values);
            }
        }
        else {
            getActivity().getContentResolver().insert(Uri.parse("content://sms/sent"), values);
        }

        // Change my sms app to the last default sms
        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, defaultSmsApp);
        getActivity().startActivity(intent);
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

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void onClick(View view) {
        if (view instanceof RadioButton) {
            switch (view.getId()) {
                case R.id.radioButtonInbox:
                    failedRadioButton.setChecked(false);
                    sentRadioButton.setChecked(false);
                    draftRadioButton.setChecked(false);
                    outboxRadioButton.setChecked(false);
                    queuedRadioButton.setChecked(false);
                    inboxRadioButton.setChecked(true);
                    break;
                case R.id.radioButtonSent:
                    inboxRadioButton.setChecked(false);
                    failedRadioButton.setChecked(false);
                    draftRadioButton.setChecked(false);
                    outboxRadioButton.setChecked(false);
                    queuedRadioButton.setChecked(false);
                    sentRadioButton.setChecked(true);
                    break;
                case R.id.radioButtonDraft:
                    inboxRadioButton.setChecked(false);
                    sentRadioButton.setChecked(false);
                    failedRadioButton.setChecked(false);
                    outboxRadioButton.setChecked(false);
                    queuedRadioButton.setChecked(false);
                    draftRadioButton.setChecked(true);
                    break;
                case R.id.radioButtonOutbox:
                    inboxRadioButton.setChecked(false);
                    sentRadioButton.setChecked(false);
                    draftRadioButton.setChecked(false);
                    failedRadioButton.setChecked(false);
                    queuedRadioButton.setChecked(false);
                    outboxRadioButton.setChecked(true);
                    break;
                case R.id.radioButtonFailed:
                    inboxRadioButton.setChecked(false);
                    sentRadioButton.setChecked(false);
                    draftRadioButton.setChecked(false);
                    outboxRadioButton.setChecked(false);
                    queuedRadioButton.setChecked(false);
                    failedRadioButton.setChecked(true);
                    break;
                case R.id.radioButtonQueued:
                    inboxRadioButton.setChecked(false);
                    sentRadioButton.setChecked(false);
                    draftRadioButton.setChecked(false);
                    outboxRadioButton.setChecked(false);
                    failedRadioButton.setChecked(false);
                    queuedRadioButton.setChecked(true);
                    break;
            }
        }
        else {
            switch (view.getId()) {
                case R.id.buttonSetDateAndTime:
                    DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show(getActivity().getFragmentManager(), "datePicker");
                    break;
                case R.id.buttonSendMessages:
                    final String myPackageName = getActivity().getPackageName();
                    if (!Telephony.Sms.getDefaultSmsPackage(getActivity()).equals(myPackageName)) {

                        //Change the default sms app to my app
                        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getActivity().getPackageName());
                        startActivityForResult(intent, SEND_ANONYMOUS_TEXT);
                    } else {
                        WriteSms(messageEditText.getText().toString(), phoneNumberEditText.getText().toString());
                    }
                    break;
                case R.id.buttonBrowse:
                    selectContact();
                    break;
            }
        }

    }

    //region Date and Time Pickers
    @Override
    public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
        calendar.set(year, monthOfYear, dayOfMonth);
        TimePickerDialog.newInstance(this, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show(getActivity().getFragmentManager(), "timePicker");
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        setDateAndTimeButton.setText(dateFormat.format(calendar.getTime()) + " " + timeFormat.format(calendar.getTime()));
    }
    //endregion
}