package uk.co.varia.nomenclator.cli.internal;

import uk.co.varia.nomenclator.cli.TitleLoader;
import uk.co.varia.nomenclator.cli.TitleLoaderProvider;

import java.nio.file.Path;
import java.util.Map;

public class TitleLoaderProviderImpl implements TitleLoaderProvider {
    public static TitleLoaderProviderImpl of() {
        return new TitleLoaderProviderImpl();
    }

    public TitleLoaderProviderImpl() {
        super();
    }

    private static final Map<String, TitleLoader> LOADERS = Map.of(
        ".csv", new CsvTitleLoader()
    );

    @Override
    public TitleLoader resolve(final Path path) {
        final var fileName = path.toString();
        return LOADERS.entrySet()
                      .stream()
                      .filter(entry -> fileName.endsWith(entry.getKey()))
                      .map(Map.Entry::getValue)
                      .findFirst()
                      .orElseThrow(() -> new UnsupportedOperationException("Unsupported file type detected: " + path));
    }
}
