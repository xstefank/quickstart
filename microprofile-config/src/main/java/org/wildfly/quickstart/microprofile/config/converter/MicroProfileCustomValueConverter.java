package org.wildfly.quickstart.microprofile.config.converter;

import org.eclipse.microprofile.config.spi.Converter;
import org.wildfly.quickstart.microprofile.config.converter.type.MicroProfileCustomValue;

public class MicroProfileCustomValueConverter implements Converter<MicroProfileCustomValue> {

    @Override
    public MicroProfileCustomValue convert(String value) {
        return new MicroProfileCustomValue(value);
    }
}
