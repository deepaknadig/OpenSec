package org.unl.cse.netgroup;

import org.onosproject.event.AbstractEvent;

/**
 * Created by dna on 7/12/16.
 */
public class NetworkEvent extends AbstractEvent<NetworkEvent.Type, String> {
    public NetworkEvent(Type type, String subject) {
        super(type, subject);
    }

    enum Type {
        NETWORK_ADDED,
        NETWORK_REMOVED,
        NETWORK_UPDATED
    }
}
