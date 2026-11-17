package io.student.rangiffler.jupiter.extension;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import io.student.rangiffler.jupiter.annotation.Theme;
import io.student.rangiffler.jupiter.annotation.ThemeMode;
import org.junit.jupiter.api.extension.ExtensionContext;

public final class ThemeSupport {
    private static final ThreadLocal<ThemeMode> CURRENT = ThreadLocal.withInitial(() -> ThemeMode.LIGHT);

    private ThemeSupport() {
    }

    public static ThemeMode current() {
        return CURRENT.get();
    }

    public static void set(ThemeMode mode) {
        CURRENT.set(mode);
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static ThemeMode resolve(ExtensionContext context) {
        return context.getTestMethod()
                .map(method -> method.getAnnotation(Theme.class))
                .or(() -> context.getTestClass().map(type -> type.getAnnotation(Theme.class)))
                .map(Theme::value)
                .orElse(ThemeMode.LIGHT);
    }

    public static void apply(BrowserContext browser) {
        ThemeMode mode = current();
        browser.addInitScript("window.localStorage.setItem('mui-mode', '%s');".formatted(mode.muiMode()));
        for (Page page : browser.pages()) {
            page.emulateMedia(new Page.EmulateMediaOptions().setColorScheme(mode.colorScheme()));
        }
    }
}
