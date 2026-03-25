package uk.co.varia.nomenclator.cli.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.varia.nomenclator.cli.Normaliser;

import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NormaliserImplTest {
    private static final Logger logger = LoggerFactory.getLogger(NormaliserImplTest.class);

    private Normaliser normaliser;
    private NomenclatorCLI cli;

    @BeforeEach
    void setUp() {
        this.normaliser = NormaliserImpl.of();
        this.cli = NomenclatorCLI.of();
    }

    @ParameterizedTest(name = "''{0}'' should normalise to ''{1}''")
    @MethodSource("titleMappings")
    void normalise_parameterised(final String input,
                                 final String expected) {
        final var result = this.normaliser.normalise(input).orElse("No match found");
        logger.debug("Normalised [{}] — expected [{}] got [{}]", input, expected, result);
        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> titleMappings() {
        return Stream.of(Arguments.of("Java engineer", "Software engineer"),
                         Arguments.of("C# engineer", "Software engineer"),
                         Arguments.of("Accountant", "Accountant"),
                         Arguments.of("Chief Accountant", "Accountant"),
                         Arguments.of("java ENGINEER", "Software engineer"),
                         Arguments.of("ACCOUNTANT", "Accountant"),
                         Arguments.of("  Java engineer  ", "Software engineer"),  // whitespace
                         Arguments.of("sOfTwArE eNgInEeR", "Software engineer"),  // mixed case
                         Arguments.of("Accountunt", "Accountant")                 // typo
        );
    }

    // --- varargs ---
    @Test
    void normalise_varargs_returnsAllNormalised() {
        final var results = this.normaliser.normalise("Java engineer", "Chief Accountant", "Accountant")
                                           .map(title -> title.orElse("No match found"))
                                           .toList();
        logger.debug("Varargs normalisation results: [{}]", results);
        assertThat(results).containsExactly("Software engineer", "Accountant", "Accountant");
    }

    // --- threshold ---
    @Test
    void normalise_belowThreshold_returnsEmpty() {
        final var normaliser = NormaliserImpl.of(Stream.of("Software engineer"), 0.99);
        final var result = normaliser.normalise("Zxqwerty");
        logger.debug("Below threshold normalisation: input [Zxqwerty] result [{}]", result);
        assertThat(result).isEmpty();
    }

    // --- stream ---
    @Test
    void normalise_stream_returnsAllNormalised() {
        final var results = this.normaliser.normalise(Stream.of("Java engineer", "C# engineer", "accountntant"))
                                           .map(title -> title.orElse("No match found"))
                                           .toList();
        logger.debug("Stream normalisation results: [{}]", results);
        assertThat(results).containsExactly("Software engineer", "Software engineer", "Accountant");
    }

    // --- custom titles list ---
    @Test
    void normalise_customTitles_returnsCorrectTitle() {
        final var normaliser = NormaliserImpl.of(Stream.of("Doctor", "Lawyer", "Pilot"));
        final var result = normaliser.normalise("Flight pilot").orElse("No match found");
        logger.debug("Custom titles normalisation: input [Flight pilot] result [{}]", result);
        assertThat(result).isEqualTo("Pilot");
    }

    // --- formatting edge cases ---
    @Test
    void normalise_emptyString_returnsATitle() {
        final var result = this.normaliser.normalise("");
        logger.debug("Empty string normalisation: result [{}]", result);
        assertThat(result).isEmpty();
    }

    @Test
    void normalise_whitespaceOnly_returnsATitle() {
        final var result = this.normaliser.normalise("   ");
        logger.debug("Whitespace only normalisation: result [{}]", result);
        assertThat(result).isEmpty();
    }

    // --- exceptions ---
    @Test
    void normalise_emptyTitlesList_throwsArgumentException() {
        logger.trace("Expecting IllegalArgumentException for empty titles list");
        assertThatThrownBy(() -> NormaliserImpl.of(Stream.empty()).normalise("Engineer"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void normalise_nullInput_throwsNullPointerException() {
        logger.debug("Expecting NullPointerException for null input");
        assertThatThrownBy(() -> this.normaliser.normalise((String) null))
                .isInstanceOf(NullPointerException.class);
    }

    // --- CSV formats ---
    @ParameterizedTest(name = "CSV format: {0}")
    @MethodSource("csvFormats")
    void loadTitles_variousFormats_returnsCorrectTitles(final String ignored,
                                                        final String csvFile) {
        final var path = Path.of("src/test/resources/csv", csvFile);
        final var normaliser = NormaliserImpl.of(CsvTitleLoader.of().load(path));
        final var result = normaliser.normalise("Marketing expert").orElse("No match found");
        logger.debug("CSV format [{}]: input [Marketing expert] result [{}]", csvFile, result);
        assertThat(result).isEqualTo("Marketing Specialist");
    }

    static Stream<Arguments> csvFormats() {
        return Stream.of(Arguments.of("multi line with header", "multi_line.csv"),
                         Arguments.of("multi line no header", "multi_line_no_header.csv"),
                         Arguments.of("single row", "single_row.csv")
        );
    }

    // --- vowel substitution ---
    @ParameterizedTest(name = "''{0}'' should score closer to ''{1}'' than ''{2}''")
    @MethodSource("vowelSubstitutionCases")
    void vowelSubstitution_scoresCloserThanVowelConsonantSubstitution(final String input,
                                                                      final String closer,
                                                                      final String further) {
        final var impl = NormaliserImpl.of(Stream.of(closer, further));
        final var result = impl.normalise(input).orElse("No match found");
        logger.debug("Vowel substitution: input [{}], closer [{}], further [{}], result [{}]",
                     input, closer, further, result);
        assertThat(result).isEqualTo(closer);
    }

    static Stream<Arguments> vowelSubstitutionCases() {
        return Stream.of(
                // 'i' substituted with 'e' (vowel-vowel) vs 'i' substituted with 'z' (vowel-consonant)
                Arguments.of("engoneer", "engineer", "engzneer"),

                // 'a' substituted with 'e' (vowel-vowel) vs 'a' substituted with 'k' (vowel-consonant)
                Arguments.of("eccountant", "accountant", "kccountant"),

                // 'u' substituted with 'o' (vowel-vowel) vs 'u' substituted with 'x' (vowel-consonant)
                Arguments.of("architoct", "architect", "architxct"),

                // 'i' substituted with 'a' (vowel-vowel) vs 'i' substituted with 'b' (vowel-consonant)
                Arguments.of("quantaty", "quantity", "quantbty")
        );
    }

    // --- keyboard proximity ---
    @ParameterizedTest(name = "''{0}'' should score closer to ''{1}'' than ''{2}''")
    @MethodSource("keyboardProximityWeightingCases")
    void keyboardProximity_scoresCloserThanNonAdjacentSubstitution(final String input,
                                                                   final String closer,
                                                                   final String further) {
        final var impl = NormaliserImpl.of(Stream.of(closer, further));
        final var result = impl.normalise(input).orElse("No match found");
        logger.debug("Keyboard proximity: input [{}], closer [{}], further [{}], result [{}]",
                     input, closer, further, result);
        assertThat(result).isEqualTo(closer);
    }

    static Stream<Arguments> keyboardProximityWeightingCases() {
        return Stream.of(
                // 'r' and 't' are adjacent on keyboard, 'r' and 'z' are not
                Arguments.of("enginert", "engineer", "enginerz"),

                // 's' and 'a' are adjacent, 's' and 'p' are not
                Arguments.of("softaare", "software", "softpare"),

                // 'n' and 'm' are adjacent, 'n' and 'q' are not
                Arguments.of("accounmant", "accountant", "accounqant"),

                // 'w' and 'e' are adjacent, 'w' and 'l' are not
                Arguments.of("architwct", "architect", "architlct"),

                // 'q' and 'w' are adjacent, 'q' and 'k' are not
                Arguments.of("qork", "work", "kork")
        );
    }

    // --- batch normalisation ---
    @Test
    void normalise_batchFromFile_returnsAllNormalised() {
        final var inputPath = Path.of("src/test/resources/csv/toNormalise.csv");
        final var result = this.cli.normalise(inputPath, null, null)
                .map(title -> title.orElse("No match found"))
                .toList();
        logger.debug("Batch from file results — {}", result);
        assertThat(result).containsExactly("Software engineer",
                "No match found",
                "Accountant",
                "Accountant",
                "Software engineer",
                "Accountant",
                "Software engineer",
                "Software engineer",
                "Accountant");
    }

    @Test
    void normaliseBatch_fromFileWithCustomTitles_returnsAllNormalised() {
        final var inputPath = Path.of("src/test/resources/csv/toNormalise.csv");
        final var titlesPath = Path.of("src/test/resources/csv/multi_line.csv");
        final var result = this.cli.normalise(inputPath, titlesPath, null)
                .map(title -> title.orElse("No match found"))
                .toList();
        logger.debug("Batch from file with custom titles results: [{}]", result);
        assertThat(result).containsExactly("DevOps Engineer",
                "No match found",
                "Accountant",
                "Accountant",
                "DevOps Engineer",
                "Accountant",
                "DevOps Engineer",
                "Software Engineer",
                "Accountant");
    }

    @Test
    void normaliseBatch_fromStream_returnsAllNormalised() {
        final var result = this.cli.normalise(Stream.of("Java engineer", "Chief Accountant", "Back end engineer"),
                        null,
                        null)
                .map(title -> title.orElse("No match found"))
                .toList();
        logger.debug("Batch from stream results: [{}]", result);
        assertThat(result).containsExactly("Software engineer",
                "Accountant",
                "Software engineer");
    }

    @Test
    void normaliseBatch_fromNonExistentFile_throwsException() {
        logger.debug("Expecting IllegalArgumentException for non-existent file");
        assertThatThrownBy(() -> this.cli.normalise(Path.of("nonexistent.csv"), null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void normaliseBatch_fromNonCsvFile_throwsException() {
        logger.debug("Expecting UnsupportedOperationException for non-CSV file");
        assertThatThrownBy(() -> this.cli.normalise(Path.of("src/test/resources/csv/toNormalise.txt"), null, null))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
