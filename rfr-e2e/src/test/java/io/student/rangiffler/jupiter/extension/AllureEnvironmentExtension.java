package io.student.rangiffler.jupiter.extension;

import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.data.db.Databases;
import io.student.rangiffler.data.db.Migrations;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.extension.ExtensionContext;

public final class AllureEnvironmentExtension implements SuiteExtension {

    public static final String RESULTS_DIRECTORY_PROPERTY = "allure.results.directory";
    private static final String DEFAULT_RESULTS_DIRECTORY = "build/allure-results";

    @Override
    public void afterSuite(ExtensionContext context) {
        Path directory = Path.of(System.getProperty(RESULTS_DIRECTORY_PROPERTY, DEFAULT_RESULTS_DIRECTORY));
        try {
            Files.createDirectories(directory);
            String body = entries().entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining(System.lineSeparator()));
            Files.writeString(directory.resolve("environment.properties"), body + System.lineSeparator(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot write Allure environment.properties to " + directory, exception);
        }
    }

    static Map<String, String> entries() {
        Map<String, String> entries = new LinkedHashMap<>();
        entries.put("frontUrl", TestConfig.frontUrl());
        entries.put("authUrl", TestConfig.authUrl());
        entries.put("apiUrl", TestConfig.apiUrl());
        entries.put("browser", TestConfig.browser());
        entries.put("headless", String.valueOf(TestConfig.headless()));
        entries.put("commit", TestConfig.commit());
        entries.put(Databases.AUTH.schema(), new Migrations(Databases.AUTH, TestConfig.authMigrations()).currentVersion());
        entries.put(Databases.API.schema(), new Migrations(Databases.API, TestConfig.apiMigrations()).currentVersion());
        return entries;
    }
}
