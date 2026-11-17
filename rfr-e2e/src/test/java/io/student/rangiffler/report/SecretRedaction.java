package io.student.rangiffler.report;

import io.qameta.allure.listener.ContainerLifecycleListener;
import io.qameta.allure.listener.TestLifecycleListener;
import io.qameta.allure.model.ExecutableItem;
import io.qameta.allure.model.FixtureResult;
import io.qameta.allure.model.Parameter;
import io.qameta.allure.model.StatusDetails;
import io.qameta.allure.model.TestResult;
import io.qameta.allure.model.TestResultContainer;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SecretRedaction implements TestLifecycleListener, ContainerLifecycleListener {
    private static final Set<Parameter.Mode> SECRET = EnumSet.of(Parameter.Mode.HIDDEN, Parameter.Mode.MASKED);
    private static final String MASK = "******";

    @Override
    public void beforeTestWrite(TestResult result) {
        Set<String> secrets = secrets(result);
        Optional.ofNullable(result.getName()).ifPresent(name -> result.setName(redact(name, secrets)));
        redact(result, secrets);
    }

    @Override
    public void beforeContainerWrite(TestResultContainer container) {
        Stream.concat(container.getBefores().stream(), container.getAfters().stream()).forEach(SecretRedaction::redact);
    }

    private static void redact(FixtureResult fixture) {
        Set<String> secrets = secrets(fixture);
        Optional.ofNullable(fixture.getName()).ifPresent(name -> fixture.setName(redact(name, secrets)));
        redact(fixture, secrets);
    }

    private static Set<String> secrets(ExecutableItem item) {
        return Stream.concat(
                        item.getParameters().stream()
                                .filter(parameter -> SECRET.contains(parameter.getMode()))
                                .map(Parameter::getValue)
                                .filter(value -> value != null && !value.isEmpty()),
                        item.getSteps().stream().flatMap(step -> secrets(step).stream()))
                .collect(Collectors.toSet());
    }

    private static void redact(ExecutableItem item, Set<String> secrets) {
        item.getParameters().forEach(parameter -> Optional.ofNullable(parameter.getValue())
                .ifPresent(value -> parameter.setValue(redact(value, secrets))));
        Optional.ofNullable(item.getStatusDetails()).ifPresent(details -> redact(details, secrets));
        item.getSteps().forEach(step -> {
            Optional.ofNullable(step.getName()).ifPresent(name -> step.setName(redact(name, secrets)));
            redact(step, secrets);
        });
    }

    private static void redact(StatusDetails details, Set<String> secrets) {
        Optional.ofNullable(details.getMessage()).ifPresent(message -> details.setMessage(redact(message, secrets)));
        Optional.ofNullable(details.getTrace()).ifPresent(trace -> details.setTrace(redact(trace, secrets)));
    }

    private static String redact(String text, Set<String> secrets) {
        return secrets.stream().reduce(text, (result, secret) -> result.replace(secret, MASK));
    }
}
