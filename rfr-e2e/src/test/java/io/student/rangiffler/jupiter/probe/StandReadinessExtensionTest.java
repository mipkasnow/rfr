package io.student.rangiffler.jupiter.probe;

import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.condition.ProbeTargetCondition;
import io.student.rangiffler.jupiter.extension.StandReadinessExtension;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("engine-probe")
@DisplayName("StandReadinessExtension")
class StandReadinessExtensionTest {

    @Test
    @DisplayName("Сервис начал отвечать через 2 с — тесты выполняются")
    void waitsUntilServiceResponds() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        AtomicBoolean frontReady = new AtomicBoolean();
        HttpServer server = startServer(requests, frontReady);
        Thread releaser = Thread.startVirtualThread(() -> {
            try {
                Thread.sleep(2000);
                frontReady.set(true);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        });
        try {
            EngineExecutionResults results = run(server.getAddress().getPort(), Map.of(
                    "rfr.standCheck", "true",
                    "rfr.standTimeout", "10"));
            assertEquals(1L, results.testEvents().succeeded().count(), "Probe test did not succeed after the service became ready");
            assertTrue(requests.get() > 0, "Stand readiness did not poll the service");
        } finally {
            releaser.interrupt();
            server.stop(0);
            clearStandProperties();
        }
    }

    @Test
    @DisplayName("Сервис не ответил за таймаут — тесты не выполняются, в ошибке есть имя сервиса")
    void failsWithServiceNameWhenTimeoutExpires() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        HttpServer server = startServer(requests, new AtomicBoolean(false));
        try {
            EngineExecutionResults results = run(server.getAddress().getPort(), Map.of(
                    "rfr.standCheck", "true",
                    "rfr.standTimeout", "2"));
            assertEquals(0L, results.testEvents().started().count(), "Probe test started despite stand timeout");
            assertTrue(failures(results, "rfr-gql-client") > 0,
                    "Timeout failure did not name rfr-gql-client");
        } finally {
            server.stop(0);
            clearStandProperties();
        }
    }

    @Test
    @DisplayName("Проверка выключена — к сервису нет ни одного запроса")
    void skipsChecksWhenDisabled() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        HttpServer server = startServer(requests, new AtomicBoolean(true));
        try {
            EngineExecutionResults results = run(server.getAddress().getPort(), Map.of(
                    "rfr.standCheck", "false",
                    "rfr.standTimeout", "2"));
            assertEquals(1L, results.testEvents().succeeded().count(), "Probe test did not succeed with stand check disabled");
            assertEquals(0, requests.get(), "Stand readiness contacted the service while disabled");
        } finally {
            server.stop(0);
            clearStandProperties();
        }
    }

    private EngineExecutionResults run(int port, Map<String, String> properties) {
        String base = "http://127.0.0.1:" + port;
        System.setProperty("rfr.dbUrl", "jdbc:mysql://127.0.0.1:" + port);
        System.setProperty("rfr.authUrl", base);
        System.setProperty("rfr.apiUrl", base);
        System.setProperty("rfr.frontUrl", base);
        properties.forEach(System::setProperty);
        return EngineTestKit.engine("junit-jupiter")
                .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
                .selectors(DiscoverySelectors.selectClass(Observed.class))
                .execute();
    }

    private long failures(EngineExecutionResults results, String messagePart) {
        return results.containerEvents().failed().stream()
                .map(event -> event.getRequiredPayload(TestExecutionResult.class))
                .flatMap(result -> result.getThrowable().stream())
                .filter(throwable -> Stream.iterate(throwable, cause -> cause != null, Throwable::getCause)
                        .anyMatch(cause -> cause.getMessage() != null && cause.getMessage().contains(messagePart)))
                .count();
    }

    private static HttpServer startServer(AtomicInteger requests, AtomicBoolean frontReady) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/.well-known/openid-configuration", exchange -> {
            requests.incrementAndGet();
            respond(exchange, 200, "{}");
        });
        server.createContext("/graphql", exchange -> {
            requests.incrementAndGet();
            respond(exchange, 200, "{\"data\":{\"__typename\":\"Query\"}}");
        });
        server.createContext("/", exchange -> {
            requests.incrementAndGet();
            if (frontReady.get()) {
                respond(exchange, 200, "ok");
            } else {
                respond(exchange, 503, "not ready");
            }
        });
        server.start();
        return server;
    }

    private static void respond(com.sun.net.httpserver.HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static void clearStandProperties() {
        System.clearProperty("rfr.dbUrl");
        System.clearProperty("rfr.authUrl");
        System.clearProperty("rfr.apiUrl");
        System.clearProperty("rfr.frontUrl");
        System.clearProperty("rfr.standCheck");
        System.clearProperty("rfr.standTimeout");
    }

    @ProbeTarget
    @ExtendWith(StandReadinessExtension.class)
    static final class Observed {
        @Test
        void ready() {
        }
    }
}
