package io.student.rangiffler.jupiter.annotation.meta;

import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Tag("ui")
@UsePlaywright
public @interface WebTest {
}
