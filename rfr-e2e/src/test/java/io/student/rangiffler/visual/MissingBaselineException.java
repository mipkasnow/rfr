package io.student.rangiffler.visual;

import org.opentest4j.TestAbortedException;

public final class MissingBaselineException extends TestAbortedException {
    public MissingBaselineException(String message) {
        super(message);
    }
}
