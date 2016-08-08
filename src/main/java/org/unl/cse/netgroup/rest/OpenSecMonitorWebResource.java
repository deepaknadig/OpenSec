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
import org.onosproject.net.device.PortStatistics;
import org.onosproject.net.flow.FlowEntry;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.statistic.Load;
import org.onosproject.net.statistic.StatisticService;
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
    final DeviceService deviceService = get(DeviceService.class);
    final FlowRuleService flowRuleService = getService(FlowRuleService.class);

    private static final String FLOWS = "flows";

    /**
     * Initialize Cumulative Statistics Variables
     */
    private long pktSentCount; // = 0;
    private long pktReceivedCount; // = 0;
    private long byteSentCount;
    private long byteReceivedCount;
    private long totalRecvPacketsDropped;
    private long totalSentPacketsDropped;
    private long totalRecvPacketErrors;
    private long totalSentPacketErrors;

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
     * @return 200 OK with All current active flow information
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("flows/all")
    public Response getFlows() {
        final Iterable<Device> devices = deviceService.getDevices();
        for (final Device device : devices) {
            final Iterable<FlowEntry> flowEntries = flowRuleService.getFlowEntries(device.id());
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
     * @return 200 OK with All current active flow count
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("flows/count")
    public Response getFlowCount() {
        ObjectNode root = mapper().createObjectNode();
        int flowsCount = flowRuleService.getFlowRuleCount();
        root.put("numFlowsInstalled", flowsCount);

        return ok(root).build();
    }

    /**
     * List Packet Statistics for all Devices.
     *
     * @return 200 OK with switch packet statistics
     */
    @GET@Produces(MediaType.APPLICATION_JSON)
    @Path("flows/packet-stats")
    public Response getPktStatistics() {
        ObjectNode root = mapper().createObjectNode();
        final Iterable<Device> devices = deviceService.getDevices();
        final ArrayNode pktStatsArray = root.putArray("packet-statistics");

        for (final Device device : devices) {
            final ObjectNode statsRoot = mapper().createObjectNode();
            statsRoot.put("device", device.id().toString());

            final ArrayNode pktsArray = statsRoot.putArray("ports");
            final Iterable<PortStatistics> portStatisticses = deviceService.getPortStatistics(device.id());

            if (portStatisticses != null) {
                for (final PortStatistics portStatistic : portStatisticses) {
                    ObjectNode inner = mapper().createObjectNode();
                    inner.put("port", portStatistic.port());
                    inner.put("packetsReceived", portStatistic.packetsReceived());
                    inner.put("packetsSent", portStatistic.packetsSent());

                    pktSentCount += portStatistic.packetsSent();
                    pktReceivedCount += portStatistic.packetsReceived();

                    pktsArray.add(inner);
                }
            }
            statsRoot.put("totalPacketsReceived", pktReceivedCount);
            statsRoot.put("totalPacketsSent", pktSentCount);
            pktStatsArray.add(statsRoot);
        }

        return ok(root).build();
    }

    /**
     * List Byte Statistics for all Devices.
     *
     * @return 200 OK with switch Byte statistics
     */
    @GET@Produces(MediaType.APPLICATION_JSON)
    @Path("flows/byte-stats")
    public Response getByteStatistics() {
        ObjectNode root = mapper().createObjectNode();
        final Iterable<Device> devices = deviceService.getDevices();
        final ArrayNode statsArray = root.putArray("byte-statistics");

        for (final Device device : devices) {
            final ObjectNode statsRoot = mapper().createObjectNode();
            statsRoot.put("device", device.id().toString());

            final ArrayNode bytesArray = statsRoot.putArray("ports");
            final Iterable<PortStatistics> portStatisticses = deviceService.getPortStatistics(device.id());

            if (portStatisticses != null) {
                for (final PortStatistics portStatistic : portStatisticses) {
                    ObjectNode inner = mapper().createObjectNode();
                    inner.put("port", portStatistic.port());
                    inner.put("bytesReceived", portStatistic.bytesReceived());
                    inner.put("bytesSent", portStatistic.bytesSent());

                    byteSentCount += portStatistic.bytesReceived();
                    byteReceivedCount += portStatistic.bytesSent();

                    bytesArray.add(inner);
                }
            }
            statsRoot.put("totalBytesReceived", byteReceivedCount);
            statsRoot.put("totalBytesSent", byteSentCount);
            statsArray.add(statsRoot);
        }

        return ok(root).build();
    }

    /**
     * List Error Statistics for all Devices.
     *
     * @return 200 OK with switch packet error statistics
     */
    @GET@Produces(MediaType.APPLICATION_JSON)
    @Path("flows/packet-error-stats")
    public Response getErrorStatistics() {
        ObjectNode root = mapper().createObjectNode();
        final Iterable<Device> devices = deviceService.getDevices();
        final ArrayNode statsArray = root.putArray("error-statistics");

        for (final Device device : devices) {
            final ObjectNode statsRoot = mapper().createObjectNode();
            statsRoot.put("device", device.id().toString());

            final ArrayNode bytesArray = statsRoot.putArray("ports");
            final Iterable<PortStatistics> portStatisticses = deviceService.getPortStatistics(device.id());

            if (portStatisticses != null) {
                for (final PortStatistics portStatistic : portStatisticses) {
                    ObjectNode inner = mapper().createObjectNode();
                    inner.put("port", portStatistic.port());
                    inner.put("recvPacketsDropped", portStatistic.packetsRxDropped());
                    inner.put("sentPacketsDropped", portStatistic.packetsTxDropped());
                    inner.put("recvPacketErrors", portStatistic.packetsRxErrors());
                    inner.put("sentPacketErrors", portStatistic.packetsTxErrors());

                    totalRecvPacketsDropped += portStatistic.packetsRxDropped();
                    totalSentPacketsDropped += portStatistic.packetsTxDropped();
                    totalRecvPacketErrors += portStatistic.packetsRxErrors();
                    totalSentPacketErrors += portStatistic.packetsTxErrors();

                    bytesArray.add(inner);
                }
            }
            statsRoot.put("totalRecvPacketsDropped", totalRecvPacketsDropped);
            statsRoot.put("totalSentPacketsDropped", totalSentPacketsDropped);
            statsRoot.put("totalRecvPacketErrors", totalRecvPacketErrors);
            statsRoot.put("totalSentPacketErrors", totalRecvPacketErrors);
            statsArray.add(statsRoot);
        }

        return ok(root).build();
    }

}
