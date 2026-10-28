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
        BrowserContext browser = BrowserContextExtension.getOrCreateBrowserContext(context);
        browser.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        BrowserContext browser = BrowserContextExtension.getOrCreateBrowserContext(context);
        if (context.getExecutionException().isEmpty()) {
            browser.tracing().stop();
            return;
        }
        Path trace = Files.createTempFile("trace-", ".zip");
        try {
            browser.tracing().stop(new Tracing.StopOptions().setPath(trace));
            try (InputStream content = Files.newInputStream(trace)) {
                Allure.attachment(
                        "trace.zip",
                        "application/zip",
                        content,
                        AttachmentOptions.withFileExtension(".zip"));
            }
        } finally {
            Files.deleteIfExists(trace);
        }
    }
}
