package io.student.rangiffler.jupiter.extension;

import com.microsoft.playwright.impl.junit.BrowserContextExtension;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

@Order(Integer.MIN_VALUE)
public final class ThemeExtension implements BeforeEachCallback, AfterEachCallback {
    @Override
    public void beforeEach(ExtensionContext context) {
        ThemeSupport.set(ThemeSupport.resolve(context));
        ThemeSupport.apply(BrowserContextExtension.getOrCreateBrowserContext(context));
    }

    @Override
    public void afterEach(ExtensionContext context) {
        ThemeSupport.clear();
    }
}
