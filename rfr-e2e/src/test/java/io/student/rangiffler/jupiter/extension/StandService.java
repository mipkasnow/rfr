package io.student.rangiffler.jupiter.extension;

import io.student.rangiffler.config.TestConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

enum StandService {
    MYSQL("MySQL", "docker compose up -d --wait") {
        @Override
        boolean ready(Duration requestTimeout) {
            int timeoutMs = Math.max(1, (int) requestTimeout.toMillis());
            int timeoutSec = Math.max(1, (int) requestTimeout.toSeconds());
            String timedUrl = TestConfig.dbUrl()
                    + "/?serverTimezone=UTC"
                    + "&connectTimeout=" + timeoutMs
                    + "&socketTimeout=" + timeoutMs;
            DriverManager.setLoginTimeout(timeoutSec);
            try (Connection connection = DriverManager.getConnection(
                    timedUrl, TestConfig.dbUser(), TestConfig.dbPassword());
                 Statement statement = connection.createStatement()) {
                statement.setQueryTimeout(timeoutSec);
                try (ResultSet resultSet = statement.executeQuery("SELECT 1")) {
                    return resultSet.next();
                }
            } catch (SQLException ignored) {
                return false;
            }
        }
    },
    AUTH("rfr-auth", "./gradlew runAll") {
        @Override
        boolean ready(Duration requestTimeout) {
            return getOk(requestTimeout, TestConfig.authUrl() + "/.well-known/openid-configuration");
        }
    },
    API("rfr-api", "./gradlew runAll") {
        @Override
        boolean ready(Duration requestTimeout) {
            byte[] body = "{\"query\":\"{__typename}\"}".getBytes(StandardCharsets.UTF_8);
            for (URI uri : expand(TestConfig.apiUrl() + "/graphql")) {
                try {
                    HttpURLConnection connection = open(uri, requestTimeout);
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Content-Length", String.valueOf(body.length));
                    try (OutputStream output = connection.getOutputStream()) {
                        output.write(body);
                    }
                    if (connection.getResponseCode() / 100 != 2) {
                        continue;
                    }
                    try (InputStream input = connection.getInputStream()) {
                        String response = new String(input.readAllBytes(), StandardCharsets.UTF_8);
                        if (response.contains("\"data\"")) {
                            return true;
                        }
                    }
                } catch (IOException ignored) {
                }
            }
            return false;
        }
    },
    FRONT("rfr-gql-client", "npm run dev") {
        @Override
        boolean ready(Duration requestTimeout) {
            return getOk(requestTimeout, TestConfig.frontUrl());
        }
    };

    private final String serviceName;
    private final String startCommand;

    StandService(String serviceName, String startCommand) {
        this.serviceName = serviceName;
        this.startCommand = startCommand;
    }

    String serviceName() {
        return serviceName;
    }

    String startCommand() {
        return startCommand;
    }

    abstract boolean ready(Duration requestTimeout);

    private static boolean getOk(Duration requestTimeout, String url) {
        for (URI uri : expand(url)) {
            try {
                HttpURLConnection connection = open(uri, requestTimeout);
                connection.setRequestMethod("GET");
                if (connection.getResponseCode() / 100 == 2) {
                    return true;
                }
            } catch (IOException ignored) {
            }
        }
        return false;
    }

    private static HttpURLConnection open(URI uri, Duration requestTimeout) throws IOException {
        URL url = uri.toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout((int) requestTimeout.toMillis());
        connection.setReadTimeout((int) requestTimeout.toMillis());
        connection.setInstanceFollowRedirects(true);
        return connection;
    }

    private static List<URI> expand(String url) {
        URI uri = URI.create(url);
        String host = uri.getHost();
        if (host == null || !"localhost".equalsIgnoreCase(host)) {
            return List.of(uri);
        }
        return resolve(host).stream()
                .map(address -> rewrite(uri, address))
                .toList();
    }

    private static List<InetAddress> resolve(String host) {
        try {
            return Arrays.asList(InetAddress.getAllByName(host));
        } catch (UnknownHostException exception) {
            throw new IllegalStateException("Cannot resolve host " + host, exception);
        }
    }

    private static URI rewrite(URI uri, InetAddress address) {
        try {
            return new URI(
                    uri.getScheme(),
                    uri.getUserInfo(),
                    literal(address),
                    uri.getPort(),
                    uri.getPath(),
                    uri.getQuery(),
                    uri.getFragment());
        } catch (URISyntaxException exception) {
            throw new IllegalStateException("Cannot rewrite " + uri + " for " + address, exception);
        }
    }

    private static String literal(InetAddress address) {
        String host = address.getHostAddress();
        int zone = host.indexOf('%');
        return zone < 0 ? host : host.substring(0, zone);
    }
}
