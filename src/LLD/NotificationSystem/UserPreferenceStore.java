package LLD.NotificationSystem;

import java.util.Set;

public interface UserPreferenceStore {
    Set<ChannelType> getEnabledChannels(User user);
}
