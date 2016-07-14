package org.unl.cse.netgroup;

import org.onosproject.event.ListenerService;
import org.onosproject.net.HostId;

import java.util.Set;

/**
 * Created by dna on 7/12/16.
 */
public interface NetworkService extends ListenerService<NetworkEvent, NetworkListener> {

    /**
     * Create a named network.
     *
     * @param network network name
     */
    void createNetwork(String network);

    /**
     * Deletes a named network.
     *
     * @param network network name
     */
    void deleteNetwork(String network);

    /**
     * Returns a set of network names.
     *
     * @return a set of network names
     */
    Set<String> getNetworks();

    /**
     * Adds a host to the given network.
     *
     * @param network network name
     * @param hostId host id
     */
    void addHost(String network, HostId hostId);

    /**
     * Removes a host from the given network.
     *
     * @param network network name
     * @param hostId host id
     */
    void removeHost(String network, HostId hostId);

    /**
     * Returns all the hosts in a network.
     *
     * @param network network name
     * @return set of host ids
     */
    Set<HostId> getHosts(String network);

}
