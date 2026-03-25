package uk.co.varia.nomenclator.cli;

import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Loads a stream of normalised titles from a source.
 */
public interface TitleLoader {
    /**
     * Load titles from the given path.
     * <p>
     * The returned stream must be closed by the caller to release the underlying file resource.
     *
     * @param path the path to load titles from.
     * @return a stream of titles.
     * @throws NullPointerException if path is null.
     * @throws IllegalArgumentException if the file does not exist or is not a CSV.
     * @throws UncheckedIOException if the file cannot be read.
     */
    Stream<String> load(Path path);
}
