package uk.co.varia.nomenclator.cli.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.varia.nomenclator.cli.Normaliser;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class NormaliserImpl implements Normaliser {
    public static NormaliserImpl of() {
        return of(DEFAULT_TITLES.stream());
    }

    public static NormaliserImpl of(final Stream<String> titles) {
        return new NormaliserImpl(titles);
    }

    private static final Logger logger = LoggerFactory.getLogger(NormaliserImpl.class);
    private static final List<String> DEFAULT_TITLES = List.of("Architect",
                                                               "Software engineer",
                                                               "Quantity surveyor",
                                                               "Accountant");
    private static final Set<Character> VOWELS = Set.of('a', 'e', 'i', 'o', 'u');

    private final List<String> titles;

    public NormaliserImpl(final Stream<String> titles) {
        super();

        logger.trace("Initialising normaliser...");
        final var materialised = titles.toList();
        if (materialised.isEmpty()) {
            throw new IllegalArgumentException("Titles cannot be empty");
        }
        this.titles = materialised;
        logger.debug("Normaliser initialised successfully.");
    }

    @Override
    public String normalise(final String title) {
        requireNonNull(title, "Title required");
        return this.titles.stream()
                          .max(Comparator.comparingDouble(candidate -> qualityScore(title, candidate)))
                          .orElseThrow(() -> new IllegalStateException("No titles loaded"));
    }

    @Override
    public Stream<String> normalise(final Stream<String> titles) {
        return titles.map(this::normalise);
    }

    @Override
    public Stream<String> normalise(final String... titles) {
        return Arrays.stream(titles)
                     .map(this::normalise);
    }

    /**
     * Computes token overlap score between input and candidate title.
     * Exact whole token matches are rewarded.
     */
    private double tokenScore(final String input,
                              final String candidate) {
        final var inputTokens = Set.of(input.toLowerCase().split("\\s+"));
        final var candidateTokens = Set.of(candidate.toLowerCase().split("\\s+"));

        final double matches = inputTokens.stream()
                                          .filter(candidateTokens::contains)
                                          .count();

        return matches / Math.max(inputTokens.size(), candidateTokens.size());
    }

    /**
     * Computes a quality score q where 1.0 is a perfect match.
     */
    private double qualityScore(final String input,
                                final String candidate) {
        final double distance = weightedLevenshtein(input, candidate);
        final double maxLen = Math.max(input.length(), candidate.length());
        final double levenshteinScore = 1.0 - (distance / maxLen);
        final double tokenScore = tokenScore(input, candidate);

        // weight token matching more heavily as it captures semantic similarity
        return (levenshteinScore * 0.4) + (tokenScore * 0.6);
    }

    /**
     * Computes weighted Levenshtein distance between two strings.
     */
    private double weightedLevenshtein(final String a,
                                       final String b) {
        final int lenA = a.length();
        final int lenB = b.length();
        final double[][] dp = new double[lenA + 1][lenB + 1];

        for (int i = 0; i <= lenA; i++) dp[i][0] = i;
        for (int j = 0; j <= lenB; j++) dp[0][j] = j;

        for (int i = 1; i <= lenA; i++) {
            for (int j = 1; j <= lenB; j++) {
                final double cost = substitutionCost(a.charAt(i - 1), b.charAt(j - 1));
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1.0,  // deletion
                                             dp[i][j - 1] + 1.0), // insertion
                                    dp[i - 1][j - 1] + cost);     // substitution
            }
        }

        return dp[lenA][lenB];
    }

    /**
     * Computes substitution cost between two characters based on weighted rules.
     */
    private double substitutionCost(final char a,
                                    final char b) {
        // case insensitivity
        final char lowerA = Character.toLowerCase(a);
        final char lowerB = Character.toLowerCase(b);

        if (lowerA == lowerB) {
            return 0.0;
        }

        // vowel-vowel substitution
        if (VOWELS.contains(lowerA) && VOWELS.contains(lowerB)) {
            return 0.5;
        }

        // keyboard proximity
        final var neighbours = KEYBOARD_PROXIMITY.get(lowerA);
        if (neighbours != null && neighbours.indexOf(lowerB) >= 0) {
            return 0.5;
        }

        return 1.0;
    }

    private static final Map<Character, String> KEYBOARD_PROXIMITY =
            Map.ofEntries(Map.entry('q', "wa"),
                          Map.entry('w', "qeasd"),
                          Map.entry('e', "wrsdf"),
                          Map.entry('r', "etdfg"),
                          Map.entry('t', "ryfgh"),
                          Map.entry('y', "tughj"),
                          Map.entry('u', "yihjk"),
                          Map.entry('i', "uojkl"),
                          Map.entry('o', "ipkl"),
                          Map.entry('p', "ol"),
                          Map.entry('a', "qwsz"),
                          Map.entry('s', "awedxz"),
                          Map.entry('d', "serfcx"),
                          Map.entry('f', "drtgvc"),
                          Map.entry('g', "ftyhbv"),
                          Map.entry('h', "gyujnb"),
                          Map.entry('j', "huikm n"),
                          Map.entry('k', "jiol m"),
                          Map.entry('l', "kop"),
                          Map.entry('z', "asx"),
                          Map.entry('x', "zsdc"),
                          Map.entry('c', "xdfv"),
                          Map.entry('v', "cfgb"),
                          Map.entry('b', "vghn"),
                          Map.entry('n', "bhjm"),
                          Map.entry('m', "njk")
    );
}
