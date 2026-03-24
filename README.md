# Nomenclator

Nomenclator is a CLI tool for normalising job titles against a list of known canonical titles.
Given an input title, it finds the closest match from the canonical list using a weighted Levenshtein
distance algorithm with token matching, keyboard proximity weighting, and vowel substitution scoring.

The default canonical titles list includes: Architect, Software engineer, Quantity surveyor, and Accountant.
A custom list can be provided at runtime via CSV.

## Modules

- [`nomenclator-cli` — the CLI tool](nomenclator-cli/README.md)

## Building & Testing

### Requirements
- Java 21+
- Maven
- Docker (for image build and functional tests)

### Building the container image (without running tests)
```shell
bin/build-qnd.sh
```

### Building the container image (with unit tests)
```shell
bin/build-tests.sh
```

### Integration tests
Integration tests spin up the Docker image inside `testContainers` and require the image to be built first.
Run them manually (e.g. inside an IDE of choice) or build a maven profile to run them.
