package io.student.rangiffler.visual;

import org.opentest4j.TestAbortedException;

public final class BaselineWrittenException extends TestAbortedException {
    public BaselineWrittenException(String message) {
        super(message);
    }
}
