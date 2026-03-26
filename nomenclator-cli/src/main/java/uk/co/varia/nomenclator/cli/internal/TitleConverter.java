package uk.co.varia.nomenclator.cli.internal;

import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

public class TitleConverter implements ITypeConverter<String> {
    @Override
    public String convert(final String value) {
        if (value.isBlank()) {
            throw new TypeConversionException("Title must not be blank");
        }
        return value;
    }
}
