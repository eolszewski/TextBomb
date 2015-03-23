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
import android.widget.TextView;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract;

import com.ericolszewski.smsbomb.R;

public class FakeTextFragment extends Fragment {

    //region Class Variables
    private static final int REQUEST_CONTACTPICKER = 1, SEND_ANONYMOUS_TEXT = 2;
    Button sendSMSButton, browseContactsButton;
    EditText phoneNumberEditText, messageEditText;
    String defaultSmsApp;
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
            browseContactsButton = (Button) layout.findViewById(R.id.buttonBrowse);
            phoneNumberEditText = (EditText) layout.findViewById(R.id.editTextPhoneNumber);
            messageEditText = (EditText) layout.findViewById(R.id.editTextMessage);

            defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(getActivity());

            sendSMSButton.setOnClickListener(new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.KITKAT)
                public void onClick(View v) {

                    final String myPackageName = getActivity().getPackageName();
                    if (!Telephony.Sms.getDefaultSmsPackage(getActivity()).equals(myPackageName)) {

                        //Change the default sms app to my app
                        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getActivity().getPackageName());
                        startActivityForResult(intent, SEND_ANONYMOUS_TEXT);
                    } else {
                        WriteSms(messageEditText.getText().toString(), phoneNumberEditText.getText().toString());
                    }
                }
            });
            browseContactsButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    selectContact();
                }
            });
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

        //Insert the message
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getActivity().getContentResolver().insert(Telephony.Sms.Sent.CONTENT_URI, values);
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
}