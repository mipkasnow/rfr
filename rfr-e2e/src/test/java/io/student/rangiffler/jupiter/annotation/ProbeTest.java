package io.student.rangiffler.jupiter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Execution(ExecutionMode.SAME_THREAD)
@ResourceLock("engine-probe")
@Tag("engine-probe")
public @interface ProbeTest {
}
