package LLD.NotificationSystem;

import java.util.Map;

class SimpleTemplateEngine implements TemplateEngine {

    @Override
    public String render(String eventType, ChannelType channelType, Map<String, String> templateData) {
        String template = lookupTemplate(eventType, channelType);
        String rendered = template;
        for (Map.Entry<String, String> entry : templateData.entrySet()) {
            rendered = rendered.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return rendered;
    }

    private String lookupTemplate(String eventType, ChannelType channelType) {
        if (eventType.equals("ORDER_CONFIRMED")) {
            switch (channelType) {
                case EMAIL:
                    return "Hi {customerName}, your order for {amount} has been confirmed. Thank you for shopping with us!";
                case SMS:
                    return "Order confirmed: {amount}. Thanks!";   // SMS: short, character-limited
                default:
                    return "Order confirmed: {amount}";
            }
        }
        return "Notification: " + eventType;
    }
}
