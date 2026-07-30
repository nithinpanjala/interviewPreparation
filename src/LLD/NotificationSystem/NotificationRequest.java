package LLD.NotificationSystem;

import java.util.Map;

public class NotificationRequest {
    private final String notificationId;
    private final User recipient;
    private final String eventType;                 // e.g. "ORDER_CONFIRMED"
    private final Map<String, String> templateData;  // values to substitute into the template

    public NotificationRequest(String string, User recipient, String eventType, Map<String, String> templateData) {
        this.notificationId = string;
        this.recipient = recipient;
        this.eventType = eventType;
        this.templateData = templateData;
    }
    public String getNotificationId(){return notificationId;}
    public User getRecipient() { return recipient; }
    public String getEventType() { return eventType; }
    public Map<String, String> getTemplateData() { return templateData; }
}

