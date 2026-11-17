package io.student.rangiffler.jupiter.probe;

import com.microsoft.playwright.Page;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.annotation.UiTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

@UiTest
@ProbeTarget
public class ThemeDefaultLightTarget {
    @Test
    void observe(Page page, TestReporter reporter) {
        page.navigate(TestConfig.frontUrl());
        reporter.publishEntry("dark", String.valueOf(ThemeDarkOnMethodTarget.isDark(page)));
    }
}
