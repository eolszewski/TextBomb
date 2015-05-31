package fragments;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ericolszewski.smsbomb.R;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class TextBombFragment extends Fragment {

    //region Google Analytics
    public static GoogleAnalytics analytics;
    public static Tracker tracker;
    //endregion

    //region Class Variables
    private static final int REQUEST_CONTACTPICKER = 1;

    Button sendSMSButton, browseContactsButton, setIntervalButton;
    EditText phoneNumberEditText, messageEditText, quantityEditText, intervalEditText;
    //endregion

    public static TextBombFragment getInstance(int position) {
        TextBombFragment textBombFragment = new TextBombFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        textBombFragment.setArguments(args);
        return textBombFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_text_bomb, container, false);
        Bundle bundle = getArguments();
        if (bundle != null) {
            sendSMSButton = (Button) layout.findViewById(R.id.buttonSendMessages);
            browseContactsButton = (Button) layout.findViewById(R.id.buttonBrowse);
            setIntervalButton = (Button) layout.findViewById(R.id.buttonInterval);
            phoneNumberEditText = (EditText) layout.findViewById(R.id.editTextRecipients);
            messageEditText = (EditText) layout.findViewById(R.id.editTextMessage);
            quantityEditText = (EditText) layout.findViewById(R.id.editTextNumber);
            intervalEditText = (EditText) layout.findViewById(R.id.editTextInterval);

            sendSMSButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (quantityEditText.getText().toString().trim().length() != 0 && Integer.parseInt(quantityEditText.getText().toString()) > 50) {
                        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                        alertDialog.setTitle("Warning");
                        alertDialog.setMessage("Sending this many messages may break your phone's ability to text for a few hours.");
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        sendSMSMessage();
                                    }
                                });
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "CANCEL",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                    }
                    else {
                        sendSMSMessage();
                    }
                }
            });
            browseContactsButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    selectContact();
                }
            });
            setIntervalButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    CustomTimePickerDialog timePickerDialog = new CustomTimePickerDialog(getActivity(), timeSetListener, 0, 0, true);
                    timePickerDialog.setTitle("Set minutes and seconds");
                    timePickerDialog.show();
                }
            });

            analytics = GoogleAnalytics.getInstance(getActivity());
            analytics.setLocalDispatchPeriod(1800);

            tracker = analytics.newTracker("UA-63547520-1"); // Replace with actual tracker/property Id
            tracker.enableExceptionReporting(true);
            tracker.enableAdvertisingIdCollection(true);
            tracker.enableAutoActivityTracking(true);
            tracker.setScreenName("main screen");
        }

        return layout;

    }

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
                Toast.makeText(getActivity(), "Please input a number greater than 0.",
                        Toast.LENGTH_LONG).show();
            } else {
                new SendMessagesTask().execute(sanitizedPhoneNumber, Integer.toString(number),
                        message, Integer.toString(interval));
            }
        } catch (Exception e) {
            if (error == 0) {
                Toast.makeText(getActivity(), "Please input a valid phone number.",
                        Toast.LENGTH_LONG).show();
            } else if (error == 1) {
                Toast.makeText(getActivity(), "Please input a message to send.",
                        Toast.LENGTH_LONG).show();
            } else if (error == 2) {
                Toast.makeText(getActivity(), "Please input a number of times to send your message.",
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
            progressDialog = ProgressDialog.show(getActivity(), "Wait", "Texts are being sent to your cell phone provider's servers.");
        }

        @Override
        protected Long doInBackground(String... params) {
            for(int count = 0; count < Integer.parseInt(params[1]); count++){
                try {
                    SystemClock.sleep(Integer.parseInt(params[3]));
                    SystemClock.sleep(500);
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(params[0], null, params[2], null, null);

                    tracker.send(new HitBuilders.EventBuilder()
                            .setLabel("Message Sent")
                            .setAction("Click")
                            .set("Message", params[2])
                            .set("Phone", params[0])
                            .build());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        protected void onPostExecute(Long result) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            progressDialog.dismiss();
            Toast.makeText(getActivity(), "Texts are being sent.",
                    Toast.LENGTH_LONG).show();
        }
    }
}