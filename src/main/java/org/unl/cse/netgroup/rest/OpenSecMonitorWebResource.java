/*
 * Copyright 2016 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unl.cse.netgroup.rest;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onosproject.codec.JsonCodec;
import org.onosproject.net.Device;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.FlowEntry;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.statistic.Load;
import org.onosproject.rest.AbstractWebResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Network Monitoring Resources for OpenSec.
 */
@Path("monitor")
public class OpenSecMonitorWebResource extends AbstractWebResource {

    private final ObjectNode root = mapper().createObjectNode();
    private final ArrayNode flowsNode = root.putArray(FLOWS);

    private static final String FLOWS = "flows";

    /**
     * Get Help Information.
     *
     * @return 200 OK
     */
    @GET
    @Path("info")
    public Response getInfo() {
        ObjectNode node = mapper().createObjectNode().put("Help", "Information");
        return ok(node).build();
    }

    /**
     * List All Current Flows.
     *
     *  @return 200 OK with All current active flow information
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("flows/all")
    public Response getFlows() {
        final Iterable<Device> devices = get(DeviceService.class).getDevices();
        for (final Device device : devices) {
            final Iterable<FlowEntry> flowEntries = get(FlowRuleService.class).getFlowEntries(device.id());
            if (flowEntries != null) {
                for (final FlowEntry entry : flowEntries) {
                    flowsNode.add(codec(FlowEntry.class).encode(entry, this));
                }
            }
        }

        return ok(root).build();
    }

    /**
     * List All Current Flow Counts.
     *
     *  @return 200 OK with All current active flow count
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("flows/count")
    public Response getFlowCount() {
        ObjectNode root = mapper().createObjectNode();
        int flowsCount = get(FlowRuleService.class).getFlowRuleCount();
        root.put("Number of Flows Installed", flowsCount);

        return ok(root).build();
    }

}
