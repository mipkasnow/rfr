package io.student.rangiffler.data.db;

import org.springframework.jdbc.core.simple.JdbcClient;

public final class Friendship implements Row {
    private final String requester;
    private final String addressee;
    private final FriendshipStatus status;
    private final JdbcClient api = Jdbc.client(Databases.API);

    public Friendship(String requester, String addressee, FriendshipStatus status) {
        this.requester = requester;
        this.addressee = addressee;
        this.status = status;
    }

    @Override
    public void create() {
        Jdbc.requireOneRow(statement("api/create-friendship.sql").update(),
                "friendship " + requester + " -> " + addressee);
    }

    @Override
    public void delete() {
        statement("api/delete-friendship.sql").update();
    }

    @Override
    public boolean exists() {
        return statement("api/count-friendship.sql").query(Long.class).single() > 0;
    }

    private JdbcClient.StatementSpec statement(String path) {
        return api.sql(Jdbc.sql(path))
                .param("requester", requester)
                .param("addressee", addressee)
                .param("status", status.name());
    }
}
