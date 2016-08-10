package org.unl.cse.netgroup;

import com.google.common.collect.HashMultimap;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.packet.Ethernet;
import org.onlab.packet.IPv4;
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

import java.util.Optional;
import java.util.Timer;

/**
 * Created by Deepak Nadig Anantha on 8/10/16.
 */

@Component(immediate = true)
public class TcpProcessor {

    private static Logger logger = LoggerFactory.getLogger(TcpProcessor.class);

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

    private final HashMultimap<DeviceId, TcpRecord> tcps = HashMultimap.create();
    private final Timer timer = new Timer("tcp-sweeper");

    private static final int PRIORITY = 128;
    private static final int DROP_PRIORITY = 129;

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
    private void processTcp(PacketContext context, Ethernet ethernet) {
        DeviceId deviceId = context.inPacket().receivedFrom().deviceId();
        MacAddress src = ethernet.getSourceMAC();
        MacAddress dst = ethernet.getDestinationMAC();
        TcpRecord tcp = new TcpRecord(src, dst);
        boolean tcped = tcps.get(deviceId).contains(tcp);

        if (tcped) {
            // TCP packet transmissions detected

        }
        else {
            // Do something
        }
    }

    // Install temporary rule for the flow (?) for TCP transmisssions between the src and dst.
    private void installTcpRules() {
        // TODO: create a TCP flow rule installer for the src/dst pair
    }

    private class TcpFlowListener implements FlowRuleListener {
        @Override
        public void event(FlowRuleEvent event) {

        }
    }

    private class TcpRecord {
        private final MacAddress src;
        private final MacAddress dst;

        public TcpRecord(MacAddress src, MacAddress dst) {
            this.src = src;
            this.dst = dst;
        }
    }

    private class TcpPacketProcessor implements PacketProcessor {

        @Override
        public void process(PacketContext context) {

        }
    }
}
