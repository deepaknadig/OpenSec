package org.unl.cse.netgroup;

import com.google.common.collect.ImmutableSet;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.event.AbstractListenerManager;
import org.onosproject.net.HostId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Created by Deepak Nadig Anantha on 7/12/16.
 */

@Component(immediate = true)
@Service

public class NetworkManager extends AbstractListenerManager<NetworkEvent, NetworkListener> implements NetworkService {
    private static Logger log = LoggerFactory.getLogger(NetworkManager.class);

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected NetworkStore store;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    protected ApplicationId applicationId;

    @Activate
    protected void activate() {
        applicationId = coreService.registerApplication("org.cse.unl.netgroup");
        log.info("Started org.cse.unl.netgroup");
    }

    @Deactivate void deactivate() {
        log.info("Stopped org.cse.unl.netgroup");
    }

    @Override
    public void createNetwork(String network) {
        checkNotNull(network, "Network name cannot be null");
        checkState(!network.contains(","),"Network cannot contain commas.");
    }

    @Override
    public void deleteNetwork(String network) {
        checkNotNull(network, "Network name cannot be null");
    }

    @Override
    public Set<String> getNetworks() {
        return ImmutableSet.of("my-network");
    }

    @Override
    public void addHost(String network, HostId hostId) {
        checkNotNull(network, "Network name cannot be null");
        checkNotNull(hostId, "HostId cannot be null");
    }

    public void removeHost(String network, HostId hostId) {

    }

    public Set<HostId> getHosts(String network) {
        return ImmutableSet.of();
    }

    private void addIntents(String network, HostId src, Set<HostId> hostsInNet) {

    }

}
