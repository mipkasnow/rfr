package io.student.rangiffler.jupiter.extension;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;
import io.student.rangiffler.config.TestConfig;

public final class BrowserOptions implements OptionsFactory {
    @Override
    public Options getOptions() {
        return new Options()
                .setBrowserName(TestConfig.browser())
                .setHeadless(TestConfig.headless())
                .setContextOptions(contextOptions())
                .setTestIdAttribute("data-test-id");
    }

    private Browser.NewContextOptions contextOptions() {
        return new Browser.NewContextOptions()
                .setLocale("en-US")
                .setViewportSize(TestConfig.viewportWidth(), TestConfig.viewportHeight());
    }
}
