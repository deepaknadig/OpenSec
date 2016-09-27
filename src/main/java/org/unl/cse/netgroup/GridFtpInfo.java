package org.unl.cse.netgroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        log.info(this.username + " " + this.srchost + ":" + this.srcport
                         + " -> " + this.dsthost + ":" + this.dstport
                         + " " + this.event);
    }


}
