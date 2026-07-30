package LLD.NotificationSystem;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationSystemDemo {
    public static void main(String[] args) throws InterruptedException {

        // wire concrete channels, each wrapped in the retry decorator
        Map<ChannelType, NotificationChannel> channels = new HashMap<>();
        channels.put(ChannelType.EMAIL, new RetryingNotificationChannel(new EmailChannel(), 3, 200));
        channels.put(ChannelType.SMS, new RetryingNotificationChannel(new SmsChannel(), 3, 200));

        TemplateEngine templateEngine = new SimpleTemplateEngine();
        InMemoryUserPreferenceStore preferenceStore = new InMemoryUserPreferenceStore();

        User customer = new User("u1", "Harsha");
        preferenceStore.setEnabledChannels(customer, EnumSet.of(ChannelType.EMAIL, ChannelType.SMS));

        ExecutorService dispatchExecutor = Executors.newFixedThreadPool(4);
        NotificationService notificationService =
                new NotificationService(channels, templateEngine, preferenceStore, dispatchExecutor);

        Map<String, String> templateData = new HashMap<>();
        templateData.put("customerName", customer.getName());
        templateData.put("amount", "$49.99");

        NotificationRequest request = new NotificationRequest(
                UUID.randomUUID().toString(), customer, "ORDER_CONFIRMED", templateData);

        System.out.println("Business logic: order confirmed, triggering notification...");
        notificationService.notify(request);
        System.out.println("Business logic: continuing immediately, not waiting on delivery.");

        notificationService.notify(request);   // duplicate — should be ignored by the dedup guard

        notificationService.shutdown();   // only so demo output flushes before JVM exits
    }
}
