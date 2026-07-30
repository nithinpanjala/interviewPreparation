package LLD.NotificationSystem;

public interface NotificationChannel {
    ChannelType getChannelType();

    void send(User recipient, String renderedMessage) throws NotificationDeliveryException;
}

