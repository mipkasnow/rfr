package io.student.rangiffler.visual;

import io.student.rangiffler.config.TestConfig;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

public final class BaselineStore {
    private final Path root;

    public BaselineStore(Path base, String os, String browser) {
        this(base.resolve(os).resolve(browser));
    }

    private BaselineStore(Path root) {
        this.root = root;
    }

    public static BaselineStore current() {
        return new BaselineStore(TestConfig.screenshotDir(), os(), TestConfig.browser());
    }

    public Optional<byte[]> read(String name) {
        Path file = file(name);
        if (Files.notExists(file)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readAllBytes(file));
        } catch (IOException failure) {
            throw new UncheckedIOException("Baseline screenshot " + file + " was not read", failure);
        }
    }

    public void write(String name, byte[] screenshot) {
        Path file = file(name);
        try {
            Files.createDirectories(file.getParent());
            Files.write(file, screenshot);
        } catch (IOException failure) {
            throw new UncheckedIOException("Baseline screenshot " + file + " was not written", failure);
        }
    }

    @Override
    public String toString() {
        return root.toAbsolutePath().normalize().toString();
    }

    private Path file(String name) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Baseline screenshot name must not be blank, got \"" + name + "\"");
        }
        return root.resolve(name + ".png");
    }

    private static String os() {
        String name = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (name.startsWith("mac")) {
            return "macos";
        }
        if (name.startsWith("windows")) {
            return "windows";
        }
        if (name.startsWith("linux")) {
            return "linux";
        }
        throw new IllegalStateException("Baseline screenshots support macOS, Windows and Linux, got os.name " + name);
    }
}
