package uk.co.varia.nomenclator.cli;

import java.util.stream.Stream;

public interface Normaliser {
    /**
     * Normalise a single title against the loaded list of known titles.
     *
     * @param title the title to normalise.
     * @return the normalised title.
     */
    String normalise(String title);

    /**
     * Normalise a stream of titles against the loaded list of known titles.
     *
     * @param titles the titles to normalise.
     * @return a stream of normalised titles.
     */
    Stream<String> normalise(Stream<String> titles);

    /**
     * Normalise a varargs array of titles against the loaded list of known titles.
     *
     * @param titles the titles to normalise.
     * @return a stream of normalised titles.
     */
    Stream<String> normalise(String... titles);
}
