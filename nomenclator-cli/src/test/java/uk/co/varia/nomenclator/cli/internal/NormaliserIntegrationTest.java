package uk.co.varia.nomenclator.cli.internal;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.MountableFile;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
class NormaliserIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(NormaliserIntegrationTest.class);
    private static final String IMAGE = "nomenclator-cli:latest";

    private String runNormalise(final String title) {
        try (final var container = new GenericContainer<>(IMAGE)) {
            container.withCommand("normalise", title)
                     .start();

            try (final var waitCmd = container.getDockerClient().waitContainerCmd(container.getContainerId())) {
                waitCmd.start().awaitStatusCode();
            }

            return container.getLogs().strip();
        }
    }

    private String runNormaliseWithCsv(final String title,
                                       final String csvResource) {
        try (final var container = new GenericContainer<>(IMAGE)) {
            container.withCopyFileToContainer(MountableFile.forClasspathResource(csvResource), "/data/titles.csv")
                     .withCommand("normalise", "--titles", "/data/titles.csv", title)
                     .start();

            try (final var waitCmd = container.getDockerClient().waitContainerCmd(container.getContainerId())) {
                waitCmd.start().awaitStatusCode();
            }

            return container.getLogs().strip();
        }
    }

    @ParameterizedTest(name = "''{0}'' should normalise to ''{1}''")
    @MethodSource("defaultListCases")
    void normalise_defaultList_returnsExpectedTitle(final String input,
                                                    final String expected) {
        final var out = runNormalise(input);
        logger.debug("Default list: input [{}], expected [{}], output [{}]", input, expected, out);
        assertThat(out).contains(expected);
    }

    static Stream<Arguments> defaultListCases() {
        return Stream.of(Arguments.of("Java engineer", "Software engineer"),
                         Arguments.of("C# engineer", "Software engineer"),
                         Arguments.of("Accountant", "Accountant"),
                         Arguments.of("Chief Accountant", "Accountant"));
    }

    @ParameterizedTest(name = "CSV format: {0}")
    @MethodSource("csvFormats")
    void normalise_customCsv_returnsExpectedTitle(final String ignored,
                                                  final String csvFile) {
        final var out = runNormaliseWithCsv("Marketing expert", "csv/" + csvFile);
        logger.debug("CSV format [{}] : output [{}]", csvFile, out);
        assertThat(out).contains("Marketing Specialist");
    }

    static Stream<Arguments> csvFormats() {
        return Stream.of(Arguments.of("multi line with header", "multi_line.csv"),
                         Arguments.of("multi line no header", "multi_line_no_header.csv"),
                         Arguments.of("single row", "single_row.csv"));
    }
}
