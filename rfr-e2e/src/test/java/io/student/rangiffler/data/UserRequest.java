package io.student.rangiffler.data;

import io.student.rangiffler.jupiter.annotation.User;

public record UserRequest(int friends,
                          int incomeInvitations,
                          int outcomeInvitations
) {
    public UserRequest {
        if (friends < 0) {
            throw new IllegalArgumentException("Friends count must be non-negative, got " + friends);
        }
        if (incomeInvitations < 0) {
            throw new IllegalArgumentException("Income invitations count must be non-negative, got " + incomeInvitations);
        }
        if (outcomeInvitations < 0) {
            throw new IllegalArgumentException("Outcome invitations count must be non-negative, got " + outcomeInvitations);
        }
    }

    public UserRequest(User user) {
        this(
                user.friends(),
                user.incomeInvitations(),
                user.outcomeInvitations()
        );
    }

    public UserFixture fixture() {
        UserFixture fixture = new SingleUserFixture(TestUser.unique());
        fixture = with(fixture, Relation.FRIEND, friends);
        fixture = with(fixture, Relation.INCOME_INVITATION, incomeInvitations);
        fixture = with(fixture, Relation.OUTCOME_INVITATION, outcomeInvitations);
        return fixture;
    }

    private UserFixture with(UserFixture fixture, Relation relation, int count) {
        return count > 0 ? new UserWithRelationsFixture(fixture, relation, count) : fixture;
    }
}
