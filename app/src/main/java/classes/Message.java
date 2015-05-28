package classes;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by ericolszewski on 5/27/15.
 */
public class Message {
    private String text;
    private Date date;
    private HashMap<String, String> recipients = new HashMap<String, String>();
    private String frequency;

    public Message(String text, Date date, HashMap<String, String> recipients, String frequency) {
        this.text = text;
        this.date = date;
        this.recipients = recipients;
        this.frequency = frequency;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public HashMap<String, String> getRecipients() {
        return recipients;
    }

    public void setRecipients(HashMap<String, String> recipients) {
        this.recipients = recipients;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }
}
