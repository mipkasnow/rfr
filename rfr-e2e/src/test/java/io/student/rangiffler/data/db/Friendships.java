package io.student.rangiffler.data.db;

import java.util.Set;

import org.springframework.jdbc.core.simple.JdbcClient;

public final class Friendships implements Rows {
    private final String profile;
    private final Set<FriendshipStatus> statuses;
    private final JdbcClient api = Jdbc.client(Databases.API);

    public Friendships(String profile, Set<FriendshipStatus> statuses) {
        this.profile = profile;
        this.statuses = Set.copyOf(statuses);
    }

    @Override
    public void delete() {
        statement("api/delete-friendships.sql").update();
    }

    @Override
    public boolean exists() {
        return statement("api/count-friendships.sql").query(Long.class).single() > 0;
    }

    private JdbcClient.StatementSpec statement(String path) {
        return api.sql(Jdbc.sql(path))
                .param("profile", profile)
                .param("statuses", statuses.stream().map(FriendshipStatus::name).toList());
    }
}
