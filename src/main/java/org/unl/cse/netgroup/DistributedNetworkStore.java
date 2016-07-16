package org.unl.cse.netgroup;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.net.HostId;
import org.onosproject.store.AbstractStore;
import org.onosproject.store.service.ConsistentMap;
import org.onosproject.store.service.MapEvent;
import org.onosproject.store.service.MapEventListener;
import org.onosproject.store.service.Serializer;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.unl.cse.netgroup.NetworkEvent.Type.*;

/**
 * Created by dna on 7/14/16.
 *
 * Network Store implementation backed by consistent map.
 */

@Component(immediate = true)
@Service
public class DistributedNetworkStore
        extends AbstractStore<NetworkEvent, NetworkStoreDelegate>
        implements NetworkStore {

    private static Logger log = LoggerFactory.getLogger(DistributedNetworkStore.class);

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected StorageService storageService;

    /*
     * Use ConsistentMap
     */
    private Map<String, Set<HostId>> networks;
    private ConsistentMap<String, Set<HostId>> nets;

    /**
     * Implement the class below
     */
    private final InternalListener listener = new InternalListener();

    @Activate
    public void activate() {
        /**
         * Use storageService.consistentMapBuilder(), and the
         * serializer: Serializer.using(KryoNamespaces.API)
         */
        nets = storageService.<String, Set<HostId>>consistentMapBuilder()
                .withSerializer(Serializer.using(KryoNamespaces.API))
                .withName("opensec-networks")
                .build();
        networks = nets.asJavaMap();

        /**
         * Use nets.addListener(listener);
         */
        nets.addListener(listener);

        log.info("Started Distributed Network Store");
    }

    @Deactivate
    public void deactivate() {
        /*
         * Use nets.removeListener()
         */
        nets.removeListener(listener);
        log.info("Stopped Distributed Network Store");
    }

    @Override
    public void putNetwork (String network) {
        networks.putIfAbsent(network, Sets.newHashSet());
    }

    @Override
    public void removeNetwork(String network) {
        networks.remove(network);
    }

    @Override
    public Set<String> getNetworks() {
        return ImmutableSet.copyOf(networks.keySet());
    }

    @Override
    public boolean addHost(String network, HostId hostId) {
        if (getHosts(network).contains(hostId)) {
            return false;
        }
        networks.computeIfPresent(network,
                                  (k, v) -> {
                                      Set<HostId> result = Sets.newHashSet(v);
                                      result.add(hostId);
                                      return result;
                                  });
        return true;
    }

    @Override
    public boolean removeHost(String network, HostId hostId) {
        if (!getHosts(network).contains(hostId)) {
            return false;
        }
        networks.computeIfPresent(network,
                                  (k, v) -> {
                                      Set<HostId> result = Sets.newHashSet(v);
                                      result.add(hostId);
                                      return result;
                                  });
        return true;
    }

    @Override
    public Set<HostId> getHosts(String network) {
        return checkNotNull(networks.get(network), "Network %s does not exist", network);
    }



    private class InternalListener implements MapEventListener<String, Set<HostId>> {
        @Override
        public void event(MapEvent<String, Set<HostId>> event) {
            final NetworkEvent.Type type;
            switch (event.type()) {
                case INSERT:
                    type = NETWORK_ADDED;
                    break;
                case UPDATE:
                    type = NETWORK_UPDATED;
                    break;
                case REMOVE:
                default:
                    type = NETWORK_REMOVED;
                    break;
            }
            notifyDelegate(new NetworkEvent(type, event.key()));
        }
    }
}
