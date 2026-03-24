package uk.co.varia.nomenclator.cli.internal;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;
import uk.co.varia.nomenclator.cli.TitleLoaderProvider;

import java.nio.file.Path;
import java.time.Duration;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Nomenclator CLI service
 */
@CommandLine.Command(name = "nomenclator-cli",
        description = "Nomenclator command line interface",
        version = {"1.0-SNAPSHOT", "Yes, it's still a snapshot..."},
        exitCodeOnVersionHelp = -1,
        exitCodeOnUsageHelp = -2,
        subcommands = {CommandLine.HelpCommand.class,
                       Normalise.class,
                       NormaliseBatch.class}
)
public class NomenclatorCLI {
    @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
    @Option(names = {"--verbose", "-v"},
            negatable = true,
            description = "Verbose output",
            defaultValue = "${env:NOMENCLATOR_CLI_VERBOSE}",
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    private boolean verbose = false;

    @Option(names = {"--snooze", "-z"},
            description = "Snooze seconds before startup",
            defaultValue = "${env:NOMENCLATOR_CLI_SNOOZE:-0}")
    int snoozeTime;

    public static NomenclatorCLI of() {
        return of(TitleLoaderProviderImpl.of());
    }

    public static NomenclatorCLI of(final TitleLoaderProvider titleLoaderProvider) {
        return new NomenclatorCLI(titleLoaderProvider);
    }

    private static final Logger logger = LoggerFactory.getLogger(NomenclatorCLI.class);

    private final TitleLoaderProvider titleLoaderProvider;

    public NomenclatorCLI(final TitleLoaderProvider titleLoaderProvider) {
        super();

        requireNonNull(titleLoaderProvider, "Title loader required");
        this.titleLoaderProvider = titleLoaderProvider;
    }

    public Stream<String> normalise(final Path inputPath,
                                    final @Nullable Path titlesPath) {
        requireNonNull(inputPath, "Input path required");
        final var toNormalise = titleLoaderProvider.resolve(inputPath).load(inputPath);

        return normalise(toNormalise, titlesPath);
    }

    public Stream<String> normalise(final Stream<String> toNormalise,
                                    final @Nullable Path titlesPath) {
        final var normaliser = titlesPath == null
                               ? NormaliserImpl.of()
                               : NormaliserImpl.of(titleLoaderProvider.resolve(titlesPath)
                                                                      .load(titlesPath));
        return normaliser.normalise(toNormalise);
    }

    public int executionStrategy(final ParseResult parseResult) {
        requireNonNull(parseResult, "Parse result required");
        try {
            if (CommandLine.printHelpIfRequested(parseResult)) {
                return 0;
            }

            setLoggingLevels(this.verbose);

            if (this.snoozeTime > 0) {
                logger.debug("Snoozing for {} seconds before startup at user request...", this.snoozeTime);
                Thread.sleep(Duration.ofSeconds(this.snoozeTime));
            }

            return new CommandLine.RunLast().execute(parseResult);
        } catch (final Exception ex) {
            System.err.printf("Nomenclator CLI error: %s%n", ex.getMessage());
            logger.error("Nomenclator CLI error", ex);
            return 600;
        } catch (final Throwable ex) {
            System.err.printf("Nomenclator CLI fatal error: %s%n", ex.getMessage());
            logger.error("Nomenclator CLI ended abnormally", ex);
            return -666;
        }
    }

    private static void setLoggingLevels(final boolean verbose) {
        final var loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        final var variaLogger = loggerContext.getLogger("uk.co.varia");
        final var nomenclatorLogger = loggerContext.getLogger("uk.co.varia.nomenclator");

        if (verbose) {
            variaLogger.setLevel(Level.INFO);
            nomenclatorLogger.setLevel(Level.TRACE);
        } else {
            variaLogger.setLevel(Level.ERROR);
            nomenclatorLogger.setLevel(Level.ERROR);
        }
    }
}
