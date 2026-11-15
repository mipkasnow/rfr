package io.student.rangiffler.visual;

import com.github.romankh3.image.comparison.ImageComparison;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.ImageComparisonState;
import io.student.rangiffler.config.TestConfig;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.Optional;
import javax.imageio.ImageIO;

public final class ScreenshotAssertions {
    private final BaselineStore store;
    private final double tolerance;
    private final boolean update;

    public ScreenshotAssertions(BaselineStore store) {
        this(store, 0);
    }

    public ScreenshotAssertions(BaselineStore store, double tolerance) {
        this(store, tolerance, TestConfig.updateScreenshots());
    }

    public ScreenshotAssertions(BaselineStore store, double tolerance, boolean update) {
        this.store = store;
        this.tolerance = tolerance;
        this.update = update;
    }

    public void assertMatches(String name, byte[] screenshot) {
        Optional<byte[]> baseline = store.read(name);
        if (baseline.isEmpty() && update) {
            store.write(name, screenshot);
            throw new BaselineWrittenException("Baseline screenshot " + name + ".png was missing and is now written to "
                    + store + ", review it in the pull request");
        }
        Optional<ScreenshotMismatchError> mismatch = mismatch(name, baseline.orElseThrow(() -> new MissingBaselineException(
                "Baseline screenshot " + name + ".png is missing in " + store
                        + ", save the approved screenshot there or rerun with -Drfr.updateScreenshots=true")), screenshot);
        if (mismatch.isPresent() && update) {
            store.write(name, screenshot);
            throw new BaselineWrittenException("Baseline screenshot " + name + ".png is rewritten in " + store
                    + " because it did not match: " + mismatch.get().getMessage() + ", review it in the pull request");
        }
        if (mismatch.isPresent()) {
            throw mismatch.get();
        }
    }

    private Optional<ScreenshotMismatchError> mismatch(String name, byte[] baseline, byte[] screenshot) {
        BufferedImage expected = image(baseline, "baseline screenshot " + name);
        BufferedImage actual = image(screenshot, "screenshot " + name);
        ImageComparisonResult result = new ImageComparison(expected, actual)
                .setAllowingPercentOfDifferentPixels(tolerance)
                .compareImages();
        if (result.getImageComparisonState() == ImageComparisonState.SIZE_MISMATCH) {
            return Optional.of(new ScreenshotMismatchError("Screenshot " + name + " is " + size(actual)
                    + " while its baseline in " + store + " is " + size(expected),
                    new ScreenshotDiff(name, baseline, screenshot, screenshot)));
        }
        if (result.getImageComparisonState() == ImageComparisonState.MISMATCH) {
            return Optional.of(new ScreenshotMismatchError(String.format(Locale.ROOT,
                    "Screenshot %s differs from its baseline in %s: more than %.2f%% of pixels changed, average colour difference %.2f%%",
                    name, store, tolerance, result.getDifferencePercent()),
                    new ScreenshotDiff(name, baseline, screenshot, png(result.getResult(), name))));
        }
        return Optional.empty();
    }

    private static byte[] png(BufferedImage image, String name) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", stream);
            return stream.toByteArray();
        } catch (IOException failure) {
            throw new UncheckedIOException("Difference image of screenshot " + name + " was not encoded", failure);
        }
    }

    private static BufferedImage image(byte[] png, String subject) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
            if (image == null) {
                throw new IllegalArgumentException("The " + subject + " is not an image ImageIO can read");
            }
            return image;
        } catch (IOException failure) {
            throw new UncheckedIOException("The " + subject + " was not decoded", failure);
        }
    }

    private static String size(BufferedImage image) {
        return image.getWidth() + "x" + image.getHeight();
    }
}
