package io.student.rangiffler.jupiter.probe;

import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.annotation.Session;
import io.student.rangiffler.jupiter.annotation.Theme;
import io.student.rangiffler.jupiter.annotation.ThemeMode;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.session.GuestSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

@UiTest
@ProbeTarget
public class ThemeSessionTarget {
    @Test
    @Theme(ThemeMode.DARK)
    void observe(TestReporter reporter, @Session GuestSession guest) {
        guest.landingPage().checkThatDarkTheme();
        reporter.publishEntry("sessionDark", "true");
    }
}
