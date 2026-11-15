package io.student.rangiffler.visual;

public record ScreenshotDiff(String name, byte[] expected, byte[] actual, byte[] diff) {
    public ScreenshotDiff {
        expected = expected.clone();
        actual = actual.clone();
        diff = diff.clone();
    }
}
