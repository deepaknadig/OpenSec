package org.unl.cse.netgroup;

import com.google.common.collect.HashMultimap;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.packet.Ethernet;
import org.onlab.packet.IPv4;
import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleEvent;
import org.onosproject.net.flow.FlowRuleListener;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketPriority;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;

/**
 * Created by Deepak Nadig Anantha on 8/10/16.
 */

@Component(immediate = true)
public class TcpProcessor {

    private static Logger logger = LoggerFactory.getLogger(TcpProcessor.class);

    private static final String MSG_TCP_TRANSMISSION = "TCP Packet Transmission detected between " + "{} and {} by {}";

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowObjectiveService flowObjectiveService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;

    private final PacketProcessor packetProcessor = new TcpPacketProcessor();
    private final FlowRuleListener flowRuleListener = new TcpFlowListener();
    private ApplicationId applicationId;

    // TCP Traffic Selector for intercepting TCP traffic
    private final TrafficSelector selector = DefaultTrafficSelector.builder()
            .matchEthType(Ethernet.TYPE_IPV4).matchIPProtocol(IPv4.PROTOCOL_TCP)
            .build();

    private final HashMultimap<DeviceId, TcpRecord> tcpHashMultimap = HashMultimap.create();
    private final Timer timer = new Timer("tcp-sweeper");

    private static final int PRIORITY = 128;
    private static final int DROP_PRIORITY = 129;
    private long tempCountHolder;


    @Activate
    public void activate() {
        applicationId = coreService.registerApplication("org.unl.cse.netgroup.tcppacketprocessor",
                                                        () -> logger.info("Starting Intercept"));
        packetService.addProcessor(packetProcessor, PRIORITY);
        flowRuleService.addListener(flowRuleListener);
        packetService.requestPackets(selector, PacketPriority.CONTROL, applicationId,
                                     Optional.empty());
        logger.info("Started TCP Packet Processor");
    }

    @Deactivate
    public void deactivate() {
        packetService.removeProcessor(packetProcessor);
        flowRuleService.removeFlowRulesById(applicationId);
        flowRuleService.removeListener(flowRuleListener);

        logger.info("Stopped TCP Packet Processor");
    }

    // Process TCP Packets
    private void processTcp(PacketContext context, Ethernet ethernet, boolean packetIncrement) {
        DeviceId deviceId = context.inPacket().receivedFrom().deviceId();
        MacAddress src = ethernet.getSourceMAC();
        MacAddress dst = ethernet.getDestinationMAC();
        long pktCount = 0;

        IPv4 ippayload = (IPv4) ethernet.getPayload();
        int srcaddr = ippayload.getSourceAddress();
        int dstaddr = ippayload.getDestinationAddress();
        IpAddress srcip = IpAddress.valueOf(srcaddr);
        IpAddress dstip = IpAddress.valueOf(dstaddr);


        TcpRecord tcpRecord = new TcpRecord(src, dst, pktCount);
        boolean tcpRecordExists = tcpHashMultimap.get(deviceId).contains(tcpRecord);

        // Create a Set to read tcpHashMultimap records.
        Set<TcpRecord> recordReader;
        recordReader = tcpHashMultimap.get(deviceId);

        if (tcpRecordExists) {
            // TCP packet transmissions detected, Perform an action on the detected flow.
//            logger.info(MSG_TCP_TRANSMISSION, new Object[]{src, dst, deviceId});
            if (packetIncrement) {

                for (TcpRecord record : recordReader) {
                    if (record.equals(tcpRecord)) {
                        tempCountHolder = record.pktCount;
                    }
                }

                tempCountHolder += 1;
                tcpRecord.pktCount = tempCountHolder;

                tcpHashMultimap.remove(deviceId, tcpRecord);
                tcpHashMultimap.put(deviceId, tcpRecord);

            }

            installFlowRules(); // TODO: Update the rule installation
            printStatistics(deviceId);
        }
        else {
            // Otherwise, Add TCP Record to the HashMultiMap
//            logger.info(MSG_TCP_TRANSMISSION, new Object[]{srcip, dstip, deviceId});
            tcpHashMultimap.put(deviceId, tcpRecord);
        }
    }

    // Install temporary rule for the flow (?) for TCP transmisssions between the src and dst.
    private void installFlowRules() {
        // TODO: create a TCP flow rule installer for the src/dst pair
    }

    // Checks whether the packet is a specified TCP packet
    private boolean isTcpPacket(Ethernet ethernet) {
        int payloadLength = ((IPv4) ethernet.getPayload()).getTotalLength();
        if (payloadLength >= 64 && ((IPv4) ethernet.getPayload()).getProtocol() == IPv4.PROTOCOL_TCP) {
            return ethernet.getEtherType() == Ethernet.TYPE_IPV4;
        }
        return false;

    }

    public class TcpRecord {
        private final MacAddress src;
        private final MacAddress dst;
        private long pktCount;

        public TcpRecord(MacAddress src, MacAddress dst, long pktCount) {
            this.src = src;
            this.dst = dst;
            this.pktCount = pktCount;
        }

        @Override
        public int hashCode() {
            return Objects.hash(src, dst);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            final TcpRecord other = (TcpRecord) object;
            return Objects.equals(this.src, other.src) && Objects.equals(this.dst, other.dst);
        }
    }

    private class TcpPacketProcessor implements PacketProcessor {
        private boolean packetCountIncr = false;

        @Override
        public void process(PacketContext context) {
            Ethernet ethernet = context.inPacket().parsed();
            if (isTcpPacket(ethernet)) {
                packetCountIncr = true;
                processTcp(context, ethernet, packetCountIncr);
            }
        }
    }

    private void printStatistics(DeviceId deviceId) {

        Set<TcpRecord> recordReader;
        recordReader = tcpHashMultimap.get(deviceId);

        // Print all TCP records
        for(TcpRecord record : recordReader){
            logger.info(" Source: " + record.src
                                + " Dest: " + record.dst
                                + " PktCount: " + record.pktCount);
        }
    }

    private class TcpFlowListener implements FlowRuleListener {
        @Override
        public void event(FlowRuleEvent event) {
            FlowRule flowRule = event.subject();
            if (event.type() == FlowRuleEvent.Type.RULE_ADDED && flowRule.appId() == applicationId.id()) {
                logger.warn("Rule Added");
            }
        }
    }

    public HashMultimap<DeviceId, TcpRecord> getTcpHashMultimap() {
        return tcpHashMultimap;
    }

}