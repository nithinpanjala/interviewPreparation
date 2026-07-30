package LLD.NotificationSystem;

import java.util.Random;

class EmailChannel implements NotificationChannel {
    private final Random random = new Random();

    @Override
    public ChannelType getChannelType() {
        return ChannelType.EMAIL;
    }

    @Override
    public void send(User recipient, String renderedMessage) throws NotificationDeliveryException {
        if (random.nextInt(10) < 3) {   // simulate ~30% provider failure, to see retry kick in
            throw new NotificationDeliveryException("Simulated email provider timeout for " + recipient.getName());
        }
        System.out.println("[EMAIL -> " + recipient.getName() + "] " + renderedMessage);
    }
}
