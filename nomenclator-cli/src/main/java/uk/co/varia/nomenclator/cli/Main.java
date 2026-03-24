package uk.co.varia.nomenclator.cli;

import picocli.CommandLine;
import uk.co.varia.nomenclator.cli.internal.NomenclatorCLI;

/**
 * The Nomenclator CLI main class.
 */
public class Main {
    /**
     * Cannot be instantiated.
     */
    private Main() {
    }

    /**
     * Application main entry point.
     *
     * @param args command line arguments.
     */
    public static void main(final String[] args) {
        final var command = NomenclatorCLI.of();
        CommandLine commandLine = new CommandLine(command);
        commandLine.setExecutionStrategy(command::executionStrategy);
        commandLine.getCommandSpec().mixinStandardHelpOptions(true);
        System.exit(commandLine.execute(args));
    }
}
