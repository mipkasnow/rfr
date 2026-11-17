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
public class ThemeDarkOnMethodTarget {
    @Test
    @Theme(ThemeMode.DARK)
    void observe(Page page, TestReporter reporter) {
        page.navigate(TestConfig.frontUrl());
        reporter.publishEntry("dark", String.valueOf(isDark(page)));
    }

    static boolean isDark(Page page) {
        return Boolean.TRUE.equals(page.evaluate("""
                () => document.documentElement.classList.contains('dark')
                    || document.documentElement.getAttribute('data-mui-color-scheme') === 'dark'
                """));
    }
}
