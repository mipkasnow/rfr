package io.student.rangiffler.jupiter.annotation;

import com.microsoft.playwright.junit.UsePlaywright;
import io.student.rangiffler.jupiter.extension.BrowserOptions;
import io.student.rangiffler.jupiter.extension.BrowserTimeoutExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Tag("ui")
@UsePlaywright(BrowserOptions.class)
@ExtendWith(BrowserTimeoutExtension.class)
public @interface UiTest {
}
