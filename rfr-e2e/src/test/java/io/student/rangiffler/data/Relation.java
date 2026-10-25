package io.student.rangiffler.data;

import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.FriendshipStatus;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public enum Relation {
    FRIEND(FriendshipStatus.ACCEPTED, true),
    INCOME_INVITATION(FriendshipStatus.PENDING, false),
    OUTCOME_INVITATION(FriendshipStatus.PENDING, true);

    private final FriendshipStatus status;
    private final boolean originIsRequester;

    public TestUser attach(TestUser origin, List<TestUser> related) {
        return switch (this) {
            case FRIEND -> origin.withFriends(related);
            case INCOME_INVITATION -> origin.withIncomeInvitations(related);
            case OUTCOME_INVITATION -> origin.withOutcomeInvitations(related);
        };
    }

    public void link(String origin, String related) {
        String requester = originIsRequester ? origin : related;
        String addressee = originIsRequester ? related : origin;
        new Friendship(requester, addressee, status).create();
    }
}
