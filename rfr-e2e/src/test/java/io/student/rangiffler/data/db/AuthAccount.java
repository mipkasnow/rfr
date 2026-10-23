package io.student.rangiffler.data.db;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

public final class AuthAccount implements Row {
    private final String username;
    private final String password;
    private final JdbcClient auth = Jdbc.client(Databases.AUTH);

    public AuthAccount(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void create() {
        String encoded = PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(password);
        Jdbc.inTransaction(Databases.AUTH, () -> {
            Jdbc.requireOneRow(auth.sql(Jdbc.sql("auth/create-user.sql"))
                    .param("username", username)
                    .param("password", encoded)
                    .update(), "auth user " + username);
            auth.sql(Jdbc.sql("auth/create-authorities.sql")).param("username", username).update();
        });
    }

    @Override
    public void delete() {
        Jdbc.inTransaction(Databases.AUTH, () -> {
            auth.sql(Jdbc.sql("auth/delete-authorities.sql")).param("username", username).update();
            auth.sql(Jdbc.sql("auth/delete-user.sql")).param("username", username).update();
        });
    }

    @Override
    public boolean exists() {
        return auth.sql(Jdbc.sql("auth/count-user.sql")).param("username", username).query(Long.class).single() > 0;
    }
}
