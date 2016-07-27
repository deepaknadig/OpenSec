package org.unl.cse.netgroup.cli;

import org.apache.karaf.shell.console.Completer;
import org.apache.karaf.shell.console.completer.StringsCompleter;
import org.onosproject.cli.AbstractShellCommand;
import org.unl.cse.netgroup.NetworkService;

import java.util.List;

/**
 * Created by dna on 7/27/16.
 * Completes Network Names
 */
public class NetworkCompleter implements Completer {

    @Override
    public int complete(String buffer, int cursor, List<String> candidates) {
        // Delegate String Completer
        StringsCompleter delegate = new StringsCompleter();

        NetworkService service = AbstractShellCommand.get(NetworkService.class);
        delegate.getStrings().addAll(service.getNetworks());

        // Delegate figures out what to offer for completion
        return delegate.complete(buffer, cursor, candidates);
    }
}
