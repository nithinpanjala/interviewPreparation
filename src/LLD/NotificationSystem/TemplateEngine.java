package LLD.NotificationSystem;

import java.util.Map;

public interface TemplateEngine {
    String render(String eventType, ChannelType channelType, Map<String, String> templateData);
}
