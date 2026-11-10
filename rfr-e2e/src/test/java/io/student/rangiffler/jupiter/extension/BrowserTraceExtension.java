package io.student.rangiffler.jupiter.extension;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.impl.junit.BrowserContextExtension;
import io.qameta.allure.Allure;
import io.qameta.allure.AttachmentOptions;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public final class BrowserTraceExtension implements BeforeEachCallback, AfterEachCallback {
    @Override
    public void beforeEach(ExtensionContext context) {
        start(BrowserContextExtension.getOrCreateBrowserContext(context));
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        stop(
                BrowserContextExtension.getOrCreateBrowserContext(context),
                context.getExecutionException().isPresent(),
                "trace.zip");
    }

    static void start(BrowserContext browser) {
        browser.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));
    }

    static void stop(BrowserContext browser, boolean failed, String attachmentName) throws Exception {
        if (!failed) {
            browser.tracing().stop();
            return;
        }
        Path trace = Files.createTempFile("trace-", ".zip");
        try {
            browser.tracing().stop(new Tracing.StopOptions().setPath(trace));
            try (InputStream content = Files.newInputStream(trace)) {
                Allure.attachment(
                        attachmentName,
                        "application/zip",
                        content,
                        AttachmentOptions.withFileExtension(".zip"));
            }
        } finally {
            Files.deleteIfExists(trace);
        }
    }
}
