package io.student.rangiffler.jupiter.annotation;

import com.microsoft.playwright.options.ColorScheme;

public enum ThemeMode {
    LIGHT,
    DARK;

    public ColorScheme colorScheme() {
        return this == DARK ? ColorScheme.DARK : ColorScheme.LIGHT;
    }

    public String muiMode() {
        return name().toLowerCase();
    }
}
