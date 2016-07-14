package org.unl.cse.netgroup;

import org.onosproject.net.HostId;
import org.onosproject.store.Store;

import java.util.Set;

/**
 * Created by dna on 7/12/16.
 */
public interface NetworkStore extends Store<NetworkEvent, NetworkStoreDelegate> {

    /**
     * Create a named network.
     *
     * @param network network name
     */
    void putNetwork(String network);

    /**
     * Removes a named network.
     *
     * @param network network name
     */
    void removeNetwork(String network);

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
     * @return true if the host was added; false if it already exists
     */
    boolean addHost(String network, HostId hostId);

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
