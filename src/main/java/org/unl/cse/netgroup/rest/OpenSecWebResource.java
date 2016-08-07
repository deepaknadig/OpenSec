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
import org.onosproject.net.topology.Topology;
import org.onosproject.net.topology.TopologyService;
import org.onosproject.rest.AbstractWebResource;
import org.unl.cse.netgroup.NetworkService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

/**
 * OpenSec REST APIs.
 */
@Path("api")
public class OpenSecWebResource extends AbstractWebResource {

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
     * Gets the Current topology.
     *
     * @return 200 OK with topology information
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("topology")
    public Response getTopology() {
        Topology topology = get(TopologyService.class).currentTopology();
        ObjectNode root = codec(Topology.class).encode(topology, this);
        return ok(root).build();
    }

    /**
     * Gets the current available networks.
     *
     * @return 200 OK with available networks inforamtion.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("topology/networks")
    public Response getNetworks() {
        ObjectNode mapper = mapper().createObjectNode();
        ArrayNode node = mapper.putArray("Networks Available");
        Set<String> networks = get(NetworkService.class).getNetworks();
        for (String network : networks) {
            node.add(network);
        }

        return ok(mapper).build();
    }

}
