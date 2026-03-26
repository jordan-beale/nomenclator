package uk.co.varia.nomenclator.cli.internal;

import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

public class ThresholdConverter implements ITypeConverter<Double> {
    @Override
    public Double convert(final String value) {
        final var v = Double.parseDouble(value);
        if (v < 0.0 || v > 1.0) {
            throw new TypeConversionException("Threshold must be between 0.0 and 1.0 inclusive, but was [%s]".formatted(value));
        }
        return v;
    }
}
