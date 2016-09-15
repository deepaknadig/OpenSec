package org.unl.cse.netgroup;

import com.google.common.collect.HashMultimap;
import org.onosproject.event.ListenerService;
import org.onosproject.net.DeviceId;

import java.util.Set;

/**
 * Created by Deepak Nadig Anantha <deepnadig@gmail.com> on 8/15/16.
 */
public interface TcpProcessorService extends ListenerService<NetworkEvent, NetworkListener> {

    /**
     * Get TCP Transmission Statistics.
     *
     * @return a Set of String TCP records
     */
    Set<String> transmissionInfo();

    /**
     * Get TCP Transmission Records Statistic.
     *
     * @return a Set of TcpRecord records
     */
    Set<TcpProcessor.TcpRecord> getRecordReader();

    /**
     * Get tcpHashMultiMap
     *
     * @return a HashMultiMap
     */
    HashMultimap<DeviceId, TcpProcessor.TcpRecord> getTcpHashMultimap();
}
