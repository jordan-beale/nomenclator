package uk.co.varia.nomenclator.cli;

import java.nio.file.Path;

/**
 * Resolves the appropriate title loader for a given path.
 */
public interface TitleLoaderProvider {
    /**
     * Resolve the appropriate loader for the given path.
     *
     * @param path the path to resolve a loader for.
     * @return the appropriate title loader.
     */
    TitleLoader resolve(Path path);
}
