package org.unl.cse.netgroup;

import org.onosproject.event.ListenerService;

import java.util.Set;

/**
 * Created by Deepak Nadig Anantha <deepnadig@gmail.com> on 8/15/16.
 */
public interface TcpProcessorService {

    /**
     * Get TCP Transmission Statistics.
     *
     * @param network network name
     */
    Set<String> transmissionInfo();

}
