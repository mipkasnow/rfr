package io.student.rangiffler.visual;

public final class ScreenshotMismatchError extends AssertionError {
    private final ScreenshotDiff diff;

    public ScreenshotMismatchError(String message, ScreenshotDiff diff) {
        super(message);
        this.diff = diff;
    }

    public ScreenshotDiff diff() {
        return diff;
    }
}
