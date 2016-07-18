package org.unl.cse.netgroup;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dna on 7/18/16.
 *
 * Logs Network Events
 */
@Component(immediate = true)
public class NetworkEventMonitor {
    private static Logger log = LoggerFactory.getLogger(NetworkEventMonitor.class);

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected NetworkService service;

    private final Listener listener = new Listener();

    @Activate
    protected void activate() {
        service.addListener(listener);
        log.info("Network Monitor Started");
    }

    @Deactivate
    protected void deactivate() {
        service.removeListener(listener);
        log.info("Network Monitor Stopped");
    }

    private class Listener implements NetworkListener {
        @Override
        public void event (NetworkEvent networkEvent) {
            log.info("{}", networkEvent);
        }
    }
}
