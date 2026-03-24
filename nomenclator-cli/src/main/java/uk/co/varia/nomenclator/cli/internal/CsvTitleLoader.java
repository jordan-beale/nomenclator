package uk.co.varia.nomenclator.cli.internal;

import uk.co.varia.nomenclator.cli.TitleLoader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class CsvTitleLoader implements TitleLoader {
    public static CsvTitleLoader of() {
        return new CsvTitleLoader();
    }

    public CsvTitleLoader() {
        super();
    }

    @Override
    public Stream<String> load(final Path path) {
        requireNonNull(path, "Path required");

        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Titles file not found: " + path);
        }

        if (!path.toString().endsWith(".csv")) {
            throw new IllegalArgumentException("Titles file must be a CSV: " + path);
        }

        try (final var lines = Files.lines(path)) {
            return lines.flatMap(line -> Arrays.stream(line.split(",")))
                    .map(String::trim)
                    .map(title -> title.replaceAll("[^a-zA-Z0-9 ]", ""))
                    .filter(line -> !line.isBlank())
                    .filter(line -> !isHeader(line))
                    .toList()
                    .stream();
        } catch (final IOException ex) {
            throw new UncheckedIOException("Failed to read titles file: " + path, ex);
        }
    }

    private static boolean isHeader(final String value) {
        requireNonNull(value, "Value required");
        return value.equalsIgnoreCase("title")
                || value.equalsIgnoreCase("job title")
                || value.equalsIgnoreCase("titles")
                || value.equalsIgnoreCase("job titles")
                || value.equalsIgnoreCase("name");
    }
}
