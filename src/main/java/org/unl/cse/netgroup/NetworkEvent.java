package org.unl.cse.netgroup;

import org.onosproject.event.AbstractEvent;
import org.onosproject.event.Event;

/**
 * Created by dna on 7/12/16.
 */
public class NetworkEvent extends AbstractEvent<NetworkEvent.Type, String> {
    protected NetworkEvent(NetworkEvent.Type type, String subject) {
        super(type, subject);
    }

    public enum Type {
        NETWORK_ADDED,
        NETWORK_REMOVED,
        NETWORK_UPDATED
    }
}
