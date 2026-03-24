package uk.co.varia.nomenclator.cli;

import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Loads a stream of normalised titles from a source.
 */
public interface TitleLoader {
    /**
     * Load titles from the given path.
     *
     * @param path the path to load titles from.
     * @return a stream of titles.
     */
    Stream<String> load(Path path);
}
