package io.student.rangiffler.data.db;

import java.util.EnumSet;
import java.util.Set;

import org.springframework.jdbc.core.simple.JdbcClient;

public final class ApiProfile implements Row {
    private final String username;
    private final JdbcClient api = Jdbc.client(Databases.API);

    public ApiProfile(String username) {
        this.username = username;
    }

    public Friendships friendships(Set<FriendshipStatus> statuses) {
        return new Friendships(
                api.sql(Jdbc.sql("api/select-profile-id.sql"))
                        .param("username", username)
                        .query(String.class)
                        .optional()
                        .orElseThrow(() -> new IllegalStateException("Profile " + username + " does not exist")),
                statuses);
    }

    @Override
    public void create() {
        Jdbc.requireOneRow(api.sql(Jdbc.sql("api/create-profile.sql"))
                .param("username", username)
                .update(), "api profile " + username);
    }

    @Override
    public void delete() {
        if (exists()) {
            friendships(EnumSet.allOf(FriendshipStatus.class)).delete();
            api.sql(Jdbc.sql("api/delete-profile.sql")).param("username", username).update();
        }
    }

    @Override
    public boolean exists() {
        return api.sql(Jdbc.sql("api/count-profile.sql")).param("username", username).query(Long.class).single() > 0;
    }
}
