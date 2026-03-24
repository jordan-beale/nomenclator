package uk.co.varia.nomenclator.cli.internal;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import static java.nio.file.Files.newBufferedWriter;
import static java.util.Objects.requireNonNull;

@Command(name = "normaliseBatch", description = "Normalise a file of titles against a list of normalised titles")
public class NormaliseBatch implements Callable<Integer> {
    @SuppressWarnings("unused")
    @ParentCommand
    private NomenclatorCLI nomenclatorCLI;

    @Option(names = {"--input", "-i"}, required = true,
            description = "Path to file containing job titles to normalise")
    private Path inputPath;

    @Option(names = {"--output", "-o"}, required = true,
            description = "Path for output")
    private Path outputPath;

    @SuppressWarnings("FieldMayBeFinal")
    @Option(names = {"--titles", "-t"},
            description = "Path to file containing normalised titles")
    private Path titles = null;

    @Override
    public Integer call() {
        requireNonNull(inputPath, "Input path required");
        requireNonNull(outputPath, "Output path required");

        System.out.printf("Normalising job titles from file [%s]...%n", this.inputPath);
        final var normalised = this.nomenclatorCLI.normalise(this.inputPath, this.titles);

        try (final var writer = newBufferedWriter(this.outputPath)) {
            normalised.forEach(title -> {
                try {
                    writer.write(title);
                    writer.newLine();
                } catch (final IOException ex) {
                    throw new RuntimeException("Failed to write normalised title", ex);
                }
            });
        } catch (final IOException ex) {
            System.err.printf("Error writing to output file [%s]: %s%n", this.outputPath, ex.getMessage());
            return 1;
        }

        System.out.printf("Normalised job titles from [%s] to [%s].%n", this.inputPath, this.outputPath);
        return 0;
    }
}
