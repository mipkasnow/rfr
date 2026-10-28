package io.student.rangiffler.jupiter.extension;

import io.student.rangiffler.config.TestConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

enum StandService {
    MYSQL("MySQL", "docker compose up -d --wait") {
        @Override
        boolean ready(Duration requestTimeout) {
            Matcher matcher = JDBC_HOST_PORT.matcher(TestConfig.dbUrl("stand"));
            if (!matcher.find()) {
                throw new IllegalStateException("Cannot parse MySQL host:port from " + TestConfig.dbUrl("stand"));
            }
            String host = matcher.group(1);
            int port = matcher.group(2) == null ? 3306 : Integer.parseInt(matcher.group(2));
            for (InetAddress address : resolve(host)) {
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(address, port), (int) requestTimeout.toMillis());
                    return true;
                } catch (IOException ignored) {
                }
            }
            return false;
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
                    connection.getResponseCode();
                    return true;
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

    private static final Pattern JDBC_HOST_PORT = Pattern.compile("^jdbc:mysql://([^/:?]+)(?::(\\d+))?");
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

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

    boolean ready() {
        return ready(REQUEST_TIMEOUT);
    }

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
