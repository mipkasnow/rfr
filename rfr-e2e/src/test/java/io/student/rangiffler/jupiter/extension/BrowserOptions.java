package io.student.rangiffler.jupiter.extension;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;
import com.microsoft.playwright.options.ReducedMotion;
import io.student.rangiffler.config.TestConfig;

public final class BrowserOptions implements OptionsFactory {
    @Override
    public Options getOptions() {
        return new Options()
                .setBrowserName(TestConfig.browser())
                .setHeadless(TestConfig.headless())
                .setLaunchOptions(new BrowserType.LaunchOptions().setSlowMo(TestConfig.slowMo()))
                .setContextOptions(contextOptions())
                .setTestIdAttribute("data-test-id");
    }

    private Browser.NewContextOptions contextOptions() {
        return new Browser.NewContextOptions()
                .setViewportSize(TestConfig.viewportWidth(), TestConfig.viewportHeight())
                .setDeviceScaleFactor(1)
                .setColorScheme(ThemeSupport.current().colorScheme())
                .setReducedMotion(ReducedMotion.REDUCE)
                .setLocale("en-US")
                .setTimezoneId("UTC");
    }
}
