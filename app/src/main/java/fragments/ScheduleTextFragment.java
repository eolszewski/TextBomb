package fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ericolszewski.smsbomb.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import classes.Message;

/**
 * Created by ericolszewski on 5/27/15.
 */
public class ScheduleTextFragment extends Fragment {

    //region Class Variables
    private ArrayList<Message> messages = new ArrayList<Message>();
    private View layout;
    private LayoutInflater inflater;
    //endregion

    public static ScheduleTextFragment getInstance(int position) {
        ScheduleTextFragment scheduleTextFragment = new ScheduleTextFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        scheduleTextFragment.setArguments(args);
        return scheduleTextFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.inflater = inflater;
        layout = inflater.inflate(R.layout.fragment_schedule_text, container, false);
        Bundle bundle = getArguments();
        if (bundle != null) {
            populateMessageList();
            populateListView();
            Button addSMSButton = (Button) layout.findViewById(R.id.buttonAddText);

            addSMSButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
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
            });
        }

        return layout;
    }

    private void populateMessageList() {
        HashMap<String, String> hashMap= new HashMap<String, String>();
        HashMap<String, String> hashMap1= new HashMap<String, String>();
        HashMap<String, String> hashMap2= new HashMap<String, String>();
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-05-29 07:00:00");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        hashMap.put("Eric Olszewski", "7138258982");

        messages.add(new Message("Wake Up!", date, hashMap, "Every Day"));

        try {
            date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-06-01 17:00:00");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        hashMap1.put("Jake Luebeck", "7138258982");

        messages.add(new Message("Take out the trash", date, hashMap1, "Every Week"));

        try {
            date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-06-02 11:45:00");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        hashMap2.put("John Jacobs", "7138258982");

        messages.add(new Message("Meet me at the gym in 15", date, hashMap2, "Every Other Day"));
    }

    private void populateListView() {
        ArrayAdapter<Message> adapter = new ListAdapter();
        ListView list = (ListView) layout.findViewById(R.id.listView);
        list.setAdapter(adapter);
    }

    private class ListAdapter extends ArrayAdapter<Message> {
        public ListAdapter() {
            super(getActivity(), R.layout.message_view, messages);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View messageView = convertView;
            if (messageView == null) {
                messageView = inflater.inflate(R.layout.message_view, parent, false);
            }

            Message currentMessage = messages.get(position);

            TextView frequencyText = (TextView) messageView.findViewById(R.id.message_frequency);
            frequencyText.setText(currentMessage.getFrequency());

            TextView recipientsText = (TextView) messageView.findViewById(R.id.message_recipients);
            recipientsText.setText(null);
            for ( String key : currentMessage.getRecipients().keySet() ) {
                recipientsText.append(String.format("%s ", key));
            }

            TextView dateText = (TextView) messageView.findViewById(R.id.message_nextOccurrence);
            dateText.setText(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(currentMessage.getDate()));

            TextView messageText = (TextView) messageView.findViewById(R.id.message_message);
            messageText.setText(currentMessage.getText());

            return messageView;
        }

    }
}