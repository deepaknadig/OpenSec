package org.unl.cse.netgroup;

import com.google.common.collect.Iterables;
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
import org.onosproject.net.intent.HostToHostIntent;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;

/**
 * Created by Deepak Nadig Anantha on 7/12/16.
 */

@Component(immediate = true)
@Service

public class NetworkManager
        extends AbstractListenerManager<NetworkEvent, NetworkListener>
        implements NetworkService {
    private static Logger log = LoggerFactory.getLogger(NetworkManager.class);

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected NetworkStore store;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IntentService intentService;

    /**
     * Using reference to the inherited 'post' method is sufficient.
     */
    private final NetworkStoreDelegate delegate = this::post;

    protected ApplicationId applicationId;

    private static final String HOST_FORMAT = "%s~%s";
    private static final String KEY_FORMAT = "%s,%s";

    @Activate
    protected void activate() {
        applicationId = coreService.registerApplication("org.cse.unl.netgroup", this::removeAllIntents);
        removeAllIntents();
        /*
         * Add the listener registry to the event dispatcher using eventDispatcher.addSink()
         * Set the delegate in the store
         */
        eventDispatcher.addSink(NetworkEvent.class, listenerRegistry);
        store.setDelegate(delegate);
        log.info("Started Network Manager");
    }

    // Remove all intents as part of application activation
    private void removeAllIntents() {
        Iterables.filter(intentService.getIntents(), i -> Objects.equals(i.appId(), applicationId))
                .forEach(intentService::withdraw);
    }

    @Deactivate
    protected void deactivate() {
        eventDispatcher.removeSink(NetworkEvent.class);
        store.unsetDelegate(delegate);
        log.info("Stopped org.cse.unl.netgroup");
    }

    @Override
    public void createNetwork(String network) {
        checkNotNull(network, "Network name cannot be null");
        checkState(!network.contains(","),"Network cannot contain commas.");

        store.putNetwork(network);
    }

    @Override
    public void deleteNetwork(String network) {
        checkNotNull(network, "Network name cannot be null");

        store.removeNetwork(network);
        removeIntents(network, null);
    }

    @Override
    public Set<String> getNetworks() {
        return store.getNetworks();
    }

    @Override
    public void addHost(String network, HostId hostId) {
        checkNotNull(network, "Network name cannot be null");
        checkNotNull(hostId, "HostId cannot be null");

        boolean hostWasAdded = store.addHost(network, hostId);
        if (hostWasAdded) {
            addIntents(network, hostId, store.getHosts(network));
        }
    }

    @Override
    public void removeHost(String network, HostId hostId) {
        checkNotNull(network, "Network name cannot be null");
        checkNotNull(hostId, "HostId cannot be null");

        boolean hostWasRemoved = store.removeHost(network, hostId);
        if (hostWasRemoved) {
            removeIntents(network, hostId);
        }
    }

    @Override
    public Set<HostId> getHosts(String network) {
        checkNotNull(network, "Network name cannot be null");

        return store.getHosts(network);
    }

    /**
     * Adds an intent between a new host and all others in the network.
     *
     * @param network network name
     * @param src the new host
     * @param hostsInNet all hosts in the network
     */
    private void addIntents(String network, HostId src, Set<HostId> hostsInNet) {

        hostsInNet.forEach(dst -> {
            if (!src.equals(dst)) {
                Intent intent = HostToHostIntent.builder()
                        .appId(applicationId)
                        .key(generateKey(network, src, dst))
                        .one(src)
                        .two(dst)
                        .build();
                intentService.submit(intent);
            }
        });
    }

    /**
     * Removes intents that involve the specified host in a network.
     *
     * @param network network name
     * @param hostId host to remove; all hosts if empty
     */
    private void removeIntents(String network,HostId hostId) {
        Iterables.filter(intentService.getIntents(), i -> matches(network, hostId, i))
                .forEach(intentService::withdraw);
    }

    /**
     * Returns ordered intent key from network and two hosts.
     *
     * @param network network name
     * @param one host one
     * @param two host two
     * @return canonical intent string key
     */
    protected Key generateKey(String network, HostId one, HostId two) {
        String hosts = one.toString().compareTo(two.toString()) < 0 ?
                format(HOST_FORMAT, one, two):format(HOST_FORMAT, two, one);
        return Key.of(format(KEY_FORMAT, network, hosts),applicationId);
    }

    /**
     * Matches an intent to a network and optional host.
     *
     * @param network network name
     * @param id optional host id, wildcard if missing
     * @param intent intent to match
     * @return true if intent matches, false otherwise
     */
    protected boolean matches(String network, HostId hostId, Intent intent) {
        if (!Objects.equals(applicationId, intent.appId())) {
            // Different App Ids
            return false;
        }

        String key = intent.key().toString();
        if (!key.startsWith(network)) {
            // Different network
            return false;
        }

        if (hostId == null) {
            // no host id specified; wildcard match
            return true;
        }

        String[] fields = key.split(",");
        // return result of id match in host portion of key
        return fields.length > 1 && fields[1].contains(hostId.toString());
    }


}
