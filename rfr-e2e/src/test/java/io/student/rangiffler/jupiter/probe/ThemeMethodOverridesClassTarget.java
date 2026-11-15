package io.student.rangiffler.jupiter.probe;

import com.microsoft.playwright.Page;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.annotation.Theme;
import io.student.rangiffler.jupiter.annotation.ThemeMode;
import io.student.rangiffler.jupiter.annotation.UiTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

@UiTest
@ProbeTarget
@Theme(ThemeMode.DARK)
public class ThemeMethodOverridesClassTarget {
    @Test
    @Theme(ThemeMode.LIGHT)
    void observe(Page page, TestReporter reporter) {
        page.navigate(TestConfig.frontUrl());
        reporter.publishEntry("dark", String.valueOf(ThemeDarkOnMethodTarget.isDark(page)));
    }
}
