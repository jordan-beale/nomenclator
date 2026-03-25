package uk.co.varia.nomenclator.cli;

import java.util.Optional;
import java.util.stream.Stream;

public interface Normaliser {
    /**
     * Normalise a single title against the loaded list of known titles.
     * <p>
     * In the event of a tie, the result is determined by the order of the titles list.
     *
     * @param title the title to normalise.
     * @return the normalised title, or empty if no match meets the quality threshold.
     * @throws NullPointerException if title is null.
     */
    Optional<String> normalise(String title);

    /**
     * Normalise a stream of titles against the loaded list of known titles.
     *
     * @param titles the titles to normalise.
     * @return a stream of normalised titles.
     * @throws NullPointerException if title is null
     */
    Stream<Optional<String>> normalise(Stream<String> titles);

    /**
     * Normalise a varargs array of titles against the loaded list of known titles.
     *
     * @param titles the titles to normalise.
     * @return a stream of normalised titles.
     * @throws NullPointerException if title is null
     */
    Stream<Optional<String>> normalise(String... titles);
}
