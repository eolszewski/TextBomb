package classes;

/**
 * Created by ericolszewski on 5/27/15.
 */
public class Message {
    private int id;
    private String text;
    private String date;
    private String updatedAt;
    private String recipients;
    private String frequency;

    public Message(int id, String text, String date, String updatedAt, String recipients, String frequency) {
        this.id = id;
        this.text = text;
        this.date = date;
        this.updatedAt = updatedAt;
        this.recipients = recipients;
        this.frequency = frequency;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getRecipients() {
        return recipients;
    }

    public void setRecipients(String recipients) {
        this.recipients = recipients;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }
}
