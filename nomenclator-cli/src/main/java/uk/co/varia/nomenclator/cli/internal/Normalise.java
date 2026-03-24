package uk.co.varia.nomenclator.cli.internal;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

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

    @Override
    public Integer call() {
        if (requireNonNull(title, "Title required").isBlank()) {
            throw new IllegalArgumentException("Title must not be blank");
        }

        System.out.printf("Normalising job title [%s]...%n", this.title);
        final var normalised = this.nomenclatorCLI.normalise(Stream.of(this.title), this.titles)
                                                  .findFirst()
                                                  .orElseThrow(() -> new IllegalStateException("Unexpected error occurred while normalising title [%s]."
                                                          .formatted(this.title)));
        System.out.printf("Normalised job title [%s] to [%s].%n", this.title, normalised);
        return 0;
    }
}
