package utilities;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by ericolszewski on 6/2/15.
 */
public class Utilities {
    private static final String TIME_PATTERN = "HH:mm";

    public static int frequencyToMilliseconds(String frequency) {
        switch (frequency) {
            case "Once":
                return 0;
            case "Every Minute":
                return 1000 * 60;
            case "Every Hour":
                return 1000 * 60 * 60;
            case "Every Day":
                return 1000 * 60 * 60 * 24;
            case "Every Week":
                return 1000 * 60 * 60 * 24 * 7;
            case "Every Month":
                return 1000 * 60 * 60 * 24 * 30;
            case "Every Year":
                return 1000 * 60 * 60 * 24 * 365;
            default:
                return 0;
        }
    }

    public static Calendar formattedTimeToCalendar(String formattedTime) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.getDefault());

        try {
            calendar.setTime(format.parse(formattedTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return calendar;
    }

    public static String calendarToFormattedString(Calendar calendar) {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_PATTERN, Locale.getDefault());
        return dateFormat.format(calendar.getTime()) + " " + timeFormat.format(calendar.getTime());
    }

    public static String checkMessageForErrors (String phoneNumber, String message, String date, String frequency){
        String sanitizedPhoneNumber, optionalPlus;

        int error = 0;

        try {
            optionalPlus = phoneNumber.substring(0, 1);
            sanitizedPhoneNumber = phoneNumber.replaceAll("[^\\d.]", "");
            if (optionalPlus.equals("+")) {
                sanitizedPhoneNumber = String.format("+%s", sanitizedPhoneNumber);
            }
            error++;

            if (message.length() == 0) {
                throw new Exception("Please input a message to send");
            }
            error++;

            if (date.equals("Set First Occurrence")) {
                throw new Exception("Please set a time to send your first message.");
            }
            error++;

            return sanitizedPhoneNumber;
        } catch (Exception e) {
            switch (error) {
                case 0:
                    return "Error: Please input a valid phone number.";
                case 1:
                    return "Error: Please input a message to send.";
                case 2:
                    return "Error: Please set a time to send your first message.";
                default:
                    return "Error: An unknown error has occurred, please try again.";
            }
        }
    }
}
