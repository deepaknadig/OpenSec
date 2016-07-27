package org.unl.cse.netgroup.cli;

import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.HostId;
import org.unl.cse.netgroup.NetworkService;

/**
 * Created by dna on 7/27/16.
 * Lists all available networks created using the CreateNetworkCommand
 */
@Command(scope = "opensec", name = "list-networks", description = "Lists all available networks")
public class ListNetworkCommand extends AbstractShellCommand {

    @Override
    protected void execute() {
        NetworkService networkService = get(NetworkService.class);

        print("Available Networks:\n");
        for (String net : networkService.getNetworks()) {
            print("%s:", net);
            for (HostId hostId : networkService.getHosts(net)) {
                print("\t%s", hostId);
            }
        }
    }
}
