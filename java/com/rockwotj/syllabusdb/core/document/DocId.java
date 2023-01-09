package com.rockwotj.syllabusdb.core.document;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

public record DocId(@Nonnull String raw) {
    private static final Pattern VALID_ID_REGEX = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*$");

    public DocId {
        if (!VALID_ID_REGEX.matcher(raw).matches()) {
            throw new IllegalArgumentException("Invalid document ID: " + raw);
        }
    }

    @Override
    public String toString() {
        return raw;
    }
}
