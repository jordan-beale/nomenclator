package uk.co.varia.nomenclator.cli.internal;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static uk.co.varia.nomenclator.cli.internal.NormaliserImpl.DEFAULT_THRESHOLD;

@Command(name = "normalise", description = "Normalise a title against a list of normalised titles")
public class Normalise implements Callable<Integer> {
    @SuppressWarnings("unused")
    @ParentCommand
    private NomenclatorCLI nomenclatorCLI;

    @SuppressWarnings("unused")
    @Parameters(paramLabel = "<title>",
                description = "Job title to normalise")
    private String title;

    @SuppressWarnings("FieldMayBeFinal")
    @Option(names = {"--titles", "-t"},
            description = "Path to file containing normalised titles")
    private Path titles = null;

    @SuppressWarnings("FieldMayBeFinal")
    @Option(names = {"--threshold", "-th"},
            description = "Threshold required to match to a normalised title")
    private Double threshold = null;

    @Override
    public Integer call() {
        if (requireNonNull(title, "Title required").isBlank()) {
            throw new IllegalArgumentException("Title must not be blank");
        }

        System.out.printf("Normalising job title [%s]...%n", this.title);
        final var normalised = this.nomenclatorCLI.normalise(Stream.of(this.title), this.titles, this.threshold)
                                                  .findFirst()
                                                  .orElseThrow(() -> new IllegalStateException("Unexpected error occurred while normalising title"))
                                                  .orElse("No match found >= threshold [%.2f]".formatted(this.threshold != null
                                                                                                         ? this.threshold
                                                                                                         : DEFAULT_THRESHOLD ));
        System.out.printf("Normalised job title [%s] to [%s].%n", this.title, normalised);
        return 0;
    }
}
