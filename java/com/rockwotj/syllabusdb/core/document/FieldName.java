package com.rockwotj.syllabusdb.core.document;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

public record FieldName(@Nonnull String raw) {
    private static final Pattern VALID_ID_REGEX = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*$");

    public FieldName {
        if (!VALID_ID_REGEX.matcher(raw).matches()) {
            throw new IllegalArgumentException("Invalid field name: " + raw);
        }
    }

    @Override
    public String toString() {
        return raw;
    }
}
