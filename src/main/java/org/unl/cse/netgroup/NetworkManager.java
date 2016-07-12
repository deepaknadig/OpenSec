package org.unl.cse.netgroup;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.event.AbstractListenerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Deepak Nadig Anantha on 7/12/16.
 */

@Component(immediate = true)
@Service

public class NetworkManager implements NetworkService {
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


}
