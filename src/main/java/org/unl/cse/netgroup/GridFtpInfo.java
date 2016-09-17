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

    private static Logger log = LoggerFactory.getLogger(GridFtpInfo.class);

    public GridFtpInfo(String srchost,
                       String dsthost,
                       String srcport,
                       String dstport,
                       String username) {
        this.srchost = srchost;
        this.dsthost = dsthost;
        this.srcport = srcport;
        this.dstport = dstport;
        this.username = username;
    }

    public void printInfo() {
        log.info("USERINFO: " + this.username);
    }


}
