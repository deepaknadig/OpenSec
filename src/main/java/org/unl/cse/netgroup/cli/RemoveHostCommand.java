package org.unl.cse.netgroup.cli;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.HostId;
import org.unl.cse.netgroup.NetworkService;

/**
 * Created by dna on 7/27/16.
 * Remove a Host from a specified Network
 */
@Command(scope = "opensec", name = "remove-host", description = "Remove a host from a network")
public class RemoveHostCommand extends AbstractShellCommand {

    @Argument(index = 0, name = "network", description = "Network Name", required = true, multiValued = false)
    String network = null;

    @Argument(index = 1, name = "hostId", description = "Host ID", required = true, multiValued = false)
    String hostId = null;

    @Override
    protected void execute() {
        NetworkService networkService = get(NetworkService.class);
        networkService.removeHost(network, HostId.hostId(hostId));
        print("Successfully removed Host %s from Network %s", network, hostId);
    }
}
