package org.unl.cse.netgroup.cli;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.unl.cse.netgroup.NetworkService;

/**
 * Created by dna on 7/27/16.
 * Removes an existing network.
 */
@Command(scope = "opensec", name = "remove-network", description = "Remove a specified network")
public class RemoveNetworkCommand extends AbstractShellCommand {

    @Argument(name = "network", index = 0, required = true, multiValued = false, description = "Network Name")
    String network = null;
    @Override
    protected void execute() {
        NetworkService networkService = get(NetworkService.class);
        networkService.deleteNetwork(network);
        print("Successfully removed network: %s", network);
    }
}
