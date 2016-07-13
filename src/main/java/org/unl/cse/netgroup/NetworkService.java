package org.unl.cse.netgroup;

import org.onosproject.event.ListenerService;
import org.onosproject.net.HostId;

import java.util.Set;

/**
 * Created by dna on 7/12/16.
 */
public interface NetworkService extends ListenerService<NetworkEvent, NetworkListener> {
    void createNetwork (String network);

    void deleteNetwork(String network);

    Set<String> getNetworks();

    void addHost(String network, HostId hostId);
}
