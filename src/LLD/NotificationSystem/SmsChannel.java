package LLD.NotificationSystem;

import java.util.Random;

class SmsChannel implements NotificationChannel {
    private final Random random = new Random();

    @Override
    public ChannelType getChannelType() {
        return ChannelType.SMS;
    }

    @Override
    public void send(User recipient, String renderedMessage) throws NotificationDeliveryException {
        if (random.nextInt(10) < 3) {
            throw new NotificationDeliveryException("Simulated SMS gateway failure for " + recipient.getName());
        }
        System.out.println("[SMS -> " + recipient.getName() + "] " + renderedMessage);
    }
}
