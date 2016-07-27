package org.unl.cse.netgroup.cli;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.unl.cse.netgroup.NetworkService;

/**
 * Created by dna on 7/27/16.
 * Create Network Command
 */
@Command(scope = "opensec", name = "create-network", description = "Create a new network")
public class CreateNetworkCmd extends AbstractShellCommand {

    @Argument(index = 0, name = "network", description = "Network Name", required = true, multiValued = false)
    String network = null;

    @Override
    protected void execute() {
        NetworkService networkService = get(NetworkService.class);
        networkService.createNetwork(network);
        print("Created a new network: %s", network);
    }

}
