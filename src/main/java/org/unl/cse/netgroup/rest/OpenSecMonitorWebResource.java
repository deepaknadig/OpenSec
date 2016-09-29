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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.HashMultimap;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.device.PortStatistics;
import org.onosproject.net.flow.FlowEntry;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.rest.AbstractWebResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unl.cse.netgroup.GridFtpInfo;
import org.unl.cse.netgroup.TcpProcessor.TcpRecord;
import org.unl.cse.netgroup.TcpProcessorService;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

/**
 * Network Monitoring Resources for OpenSec.
 */
@Path("monitor")
public class OpenSecMonitorWebResource extends AbstractWebResource {

    private final ObjectNode root = mapper().createObjectNode();
    private final ArrayNode flowsNode = root.putArray(FLOWS);
    final DeviceService deviceService = get(DeviceService.class);
    final FlowRuleService flowRuleService = getService(FlowRuleService.class);

    private static Logger log = LoggerFactory.getLogger(OpenSecMonitorWebResource.class);

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
    private static GridFtpInfo ftpInfo;

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
    @GET
    @Produces(MediaType.APPLICATION_JSON)
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

//    /**
//     * List TCP Transmission Statistics for all Devices.
//     *
//     * @return 200 OK with tcp transmission statistics
//     */
//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    @Path("tcp")
//    public Response getTcpStatistics() {
//
//        TcpProcessorService processor = get(TcpProcessorService.class);
//        Set<String> records = processor.transmissionInfo();
//
//        ObjectNode root = mapper().createObjectNode();
//        final ArrayNode statsArray = root.putArray("tcp-transmission-stats");
//
//        for (final String record : records) {
//            final ObjectNode statsRoot = mapper().createObjectNode();
//            statsRoot.put("Record", record);
//
//            statsArray.add(statsRoot);
//        }
//
//        return ok(root).build();
//    }

    /**
     * List TCP Transmission Statistics for all Devices.
     *
     * @return 200 OK with tcp transmission statistics
     */
    @GET
    @Path("tcpinfo")
    public Response getTcpTwo() {
        ObjectNode root = mapper().createObjectNode();
        root.put("measurement", "gridftp-transfers");

        ObjectNode tagContents = mapper().createObjectNode();
        tagContents.put("host", "server01");
        tagContents.put("region", "us-midwest");

        root.set("tags",tagContents);
        ObjectNode fieldContents = mapper().createObjectNode();

        TcpProcessorService processor = get(TcpProcessorService.class);
        HashMultimap<DeviceId, TcpRecord> map = processor.getTcpHashMultimap();

        for (DeviceId id : map.keys()) {
            ObjectNode switchesContents = mapper().createObjectNode();
            ObjectNode deviceContents = mapper().createObjectNode();

            for (Map.Entry<DeviceId, Collection<TcpRecord>> entry : map.asMap().entrySet()) {
                DeviceId d = entry.getKey();
                Collection<TcpRecord> v = entry.getValue();
                ArrayNode deviceContentArray = deviceContents.putArray("Devices");
                for (TcpRecord t : v) {
                    deviceContentArray.add(String.valueOf(t.getSrc()));
                    deviceContentArray.add(String.valueOf(t.getDst()));
                    deviceContentArray.add(String.valueOf(t.getPktCount()));
                    deviceContentArray.add(String.valueOf(t.getByteCount()));
                }

            }
            switchesContents.set(String.valueOf(id), deviceContents);
            fieldContents.set("Devices", switchesContents);
        }

        fieldContents.put("field1", 9);
        fieldContents.put("field2", 0.64);
        root.set("fields", fieldContents);

        return ok(root).build();
    }

    /**
     * Obtains GridFTP transfer information
     * Instructions description:
     * <br>
     * Criteria description:
     *
     * @return status of the request - CREATED if the JSON is correct,
     * BAD_REQUEST if the JSON is invalid
     */
    @POST
    @Path("tcp")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postedFlows(InputStream stream) throws IOException {
    // TODO Exception Handling
//        try {
//            ObjectNode jsonTree = (ObjectNode) mapper().readTree(stream);
//        } catch (IOException e) {
//            throw new IllegalArgumentException(e);
//        }

        ftpInfo = jsonToGridftp(stream);
        //ftpInfo.logInfo();
        ftpInfo.testCode();



        //log.info(ftpInfo.transferInfo().toString());

        return Response.ok(root).build();
    }

    private GridFtpInfo jsonToGridftp(InputStream stream) {
        JsonNode node;
        try {
            node = mapper().readTree(stream);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to parse GridFTP application POST request.", e);
        }

        String srchost = node.path("srchost").asText(null);
        String dsthost = node.path("dsthost").asText(null);
        String srcport = node.path("srcport").asText(null);
        String dstport = node.path("dstport").asText(null);
        String username = node.path("username").asText(null);
        String event = node.path("event").asText(null);

        if (srchost != null && dsthost != null && srcport != null && dstport != null && username != null && event != null) {
            return new GridFtpInfo(srchost, dsthost, srcport, dstport, username, event);
        }
        else {
            throw new IllegalArgumentException("Arguments cannot be null");
        }
    }

    /**
     * List GridFTP Transmission Statistics for all Servers.
     *
     * @return 200 OK with gridftp transmission statistics
     */
    @GET
    @Path("grid-transfer-stats")
    public Response getGridTransferStats() {
        ObjectNode root = mapper().createObjectNode();
        root.put("measurement", "gridftp-stats");

        ObjectNode tagContents = mapper().createObjectNode();
        tagContents.put("host", "red");
        tagContents.put("region", "us-midwest");

        root.set("tags",tagContents);
        ObjectNode fieldContents = mapper().createObjectNode();

        // TODO Needs GridFtpInfoService Interface for access
        // The code below won't return anything
        GridFtpInfo ftpInfo = get(GridFtpInfo.class);

        fieldContents.put("USCMSPOOL", ftpInfo.transferInfo().get("USCMSPOOL"));
        fieldContents.put("CMSPROD", ftpInfo.transferInfo().get("CMSPROD"));
        fieldContents.put("LCGADMIN", ftpInfo.transferInfo().get("LCGADMIN"));
        fieldContents.put("CMSPHEDEX", ftpInfo.transferInfo().get("CMSPHEDEX"));
        fieldContents.put("OTHERS", ftpInfo.transferInfo().get("OTHERS"));

        root.set("fields", fieldContents);

        return ok(root).build();
    }


}
