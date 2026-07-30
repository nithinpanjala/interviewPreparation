package LLD.NotificationSystem;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserPreferenceStore implements UserPreferenceStore {
    private final Map<String, Set<ChannelType>> enabledChannelsByUserId = new ConcurrentHashMap<>();

    public void setEnabledChannels(User user, Set<ChannelType> channels) {
        enabledChannelsByUserId.put(user.getUserId(), channels);
    }

    @Override
    public Set<ChannelType> getEnabledChannels(User user) {
        return enabledChannelsByUserId.getOrDefault(user.getUserId(), Collections.emptySet());
    }
}

