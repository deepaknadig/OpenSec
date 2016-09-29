package org.unl.cse.netgroup;

import com.google.common.collect.HashMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
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

    private long transfersUsCmsPool;
    private long transfersCmsProd;
    private long transfersLcgAdmin;
    private long transfersCmsPhedex;
    private long transfersOthers;


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

        // TODO Exception Handling
        if (this.event.equals("STARTUP") || this.event.equals("UPDATE")) {
            if (this.username.matches("uscms(.*)")) {
                usCmsPoolMap.put(this.username, elementBuilder.toString());

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
        else if (this.event.equals("SHUTDOWN")) {
            if (this.username.matches("uscms(.*)")) {
                usCmsPoolMap.remove(this.username, elementBuilder.toString());
            } else if (this.username.matches("cmsprod(.*)")) {
                cmsProdlMap.remove(this.username, element);
            } else if (this.username.matches("lcgadmin(.*)")) {
                lcgAdminMap.remove(this.username, element);
            } else if (this.username.matches("cmsphedex(.*)")) {
                cmsPhedexMap.remove(this.username, element);
            } else {
                otherMap.remove(this.username, element);
            }
        }

    }

    public HashMap<String, Long> transferInfo() {
        transfersUsCmsPool = usCmsPoolMap.size();
        transfersCmsProd = cmsProdlMap.size();
        transfersLcgAdmin = lcgAdminMap.size();
        transfersCmsPhedex = cmsPhedexMap.size();
        transfersOthers = otherMap.size();

        HashMap<String, Long> transferStatsList = new HashMap<>();
        transferStatsList.put("USCMSPOOL", transfersUsCmsPool);
        transferStatsList.put("CMSPROD", transfersCmsProd);
        transferStatsList.put("LCGADMIN", transfersLcgAdmin);
        transferStatsList.put("CMSPHEDEX", transfersCmsPhedex);
        transferStatsList.put("OTHERS", transfersOthers);
        return transferStatsList;
    }


}
