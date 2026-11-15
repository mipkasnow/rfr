package io.student.rangiffler.jupiter.extension;

import io.qameta.allure.Allure;
import io.qameta.allure.AttachmentOptions;
import io.student.rangiffler.visual.ScreenshotDiff;
import io.student.rangiffler.visual.ScreenshotMismatchError;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.MultipleFailuresError;

public final class ScreenshotDiffExtension implements AfterTestExecutionCallback {
    @Override
    public void afterTestExecution(ExtensionContext context) {
        context.getExecutionException().stream()
                .flatMap(ScreenshotDiffExtension::mismatches)
                .distinct()
                .map(ScreenshotMismatchError::diff)
                .forEach(ScreenshotDiffExtension::attach);
    }

    private static Stream<ScreenshotMismatchError> mismatches(Throwable failure) {
        List<Throwable> nested = failure instanceof MultipleFailuresError multiple ? multiple.getFailures() : List.of();
        return Stream.of(
                        Stream.of(failure).filter(ScreenshotMismatchError.class::isInstance).map(ScreenshotMismatchError.class::cast),
                        Stream.concat(Stream.concat(nested.stream(), Stream.of(failure.getSuppressed())),
                                        Optional.ofNullable(failure.getCause()).stream())
                                .flatMap(ScreenshotDiffExtension::mismatches))
                .flatMap(stream -> stream);
    }

    private static void attach(ScreenshotDiff diff) {
        Allure.attachment("Screenshot diff " + diff.name(), "application/vnd.allure.image.diff",
                "{\"expected\":\"%s\",\"actual\":\"%s\",\"diff\":\"%s\"}"
                        .formatted(dataUrl(diff.expected()), dataUrl(diff.actual()), dataUrl(diff.diff())),
                AttachmentOptions.withFileExtension("imagediff"));
    }

    private static String dataUrl(byte[] png) {
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(png);
    }
}
