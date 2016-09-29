package org.unl.cse.netgroup;

import com.google.common.collect.HashMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

/**
 * Created by Deepak Nadig Anantha <deepnadig@gmail.com> on 9/17/16.
 */
public class GridFtpInfo {
    private String srchost;
    private String dsthost;
    private String srcport;
    private String dstport;
    private String username;
    private String event;

    private static Logger log = LoggerFactory.getLogger(GridFtpInfo.class);

    private static HashMultimap<String, String> usCmsPoolMap = HashMultimap.create();
    private static HashMultimap<String, HashSet<String>> cmsProdlMap = HashMultimap.create();
    private static HashMultimap<String, HashSet<String>> lcgAdminMap = HashMultimap.create();
    private static HashMultimap<String, HashSet<String>> cmsPhedexMap = HashMultimap.create();
    private static HashMultimap<String, HashSet<String>> otherMap = HashMultimap.create();

    private HashSet<String> element = new HashSet<>();
    private StringBuilder elementBuilder = new StringBuilder();


    public GridFtpInfo(String srchost,
                       String dsthost,
                       String srcport,
                       String dstport,
                       String username,
                       String event) {
        this.srchost = srchost;
        this.dsthost = dsthost;
        this.srcport = srcport;
        this.dstport = dstport;
        this.username = username;
        this.event = event;
    }

    public String getSrchost() {
        return srchost;
    }

    public String getDsthost() {
        return dsthost;
    }

    public String getSrcport() {
        return srcport;
    }

    public String getDstport() {
        return dstport;
    }

    public String getUsername() {
        return username;
    }

    public String getEvent() {
        return event;
    }

    public void logInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.username).append(" ").append(this.srchost)
                    .append(":").append(this.srcport)
                    .append(" -> ").append(this.dsthost)
                    .append(":").append(this.dstport)
                    .append(" ").append(this.event);
        //log.info(sb.toString());
    }

    public void testCode() {
        elementBuilder.append(this.srchost)
                .append(":").append(this.srcport)
                .append(",").append(this.dsthost)
                .append(":").append(this.dstport);
        element.add(elementBuilder.toString());

        if (this.event.equals("STARTUP")) {
            if (this.username.matches("uscms(.*)")) {
                usCmsPoolMap.put(this.username, elementBuilder.toString());
//                log.info("USCMSPOOL: " + usCmsPoolMap);
//                log.info("USCMSPOOL keySet " + String.valueOf(usCmsPoolMap.keySet().size()));
//                log.info("USCMSPOOL asMap " + String.valueOf(usCmsPoolMap.asMap().size()));
//                log.info("USCMSPOOL Size " + String.valueOf(usCmsPoolMap.size()));
//                log.info("USCMSPOOL Entries " + String.valueOf(usCmsPoolMap.entries().size()));

            } else if (this.username.matches("cmsprod(.*)")) {
                cmsProdlMap.put(this.username, element);

//                log.info("CMSPROD " + String.valueOf(cmsProdlMap.entries().size()));
            } else if (this.username.matches("lcgadmin(.*)")) {
                lcgAdminMap.put(this.username, element);

//                log.info("LCGADMIN " + String.valueOf(lcgAdminMap.entries().size()));
            } else if (this.username.matches("cmsphedex(.*)")) {
                cmsPhedexMap.put(this.username, element);

//                log.info("CMSPHEDEX " + String.valueOf(cmsPhedexMap.entries().size()));
            } else {
                otherMap.put(this.username, element);

//                log.info("OTHERS: " + String.valueOf(otherMap.entries().size()));
            }
        }

    }


}
