package io.student.rangiffler.domain;

import java.util.List;
import java.util.stream.Stream;

public enum FriendStatus {
    NOT_FRIEND(List.of(PeopleAction.ADD), false),
    FRIEND(List.of(PeopleAction.REMOVE), false),
    INVITATION_SENT(List.of(PeopleAction.CANCEL), true),
    INVITATION_RECEIVED(List.of(PeopleAction.ACCEPT, PeopleAction.DECLINE), false);

    private final List<PeopleAction> actions;
    private final boolean waiting;

    FriendStatus(List<PeopleAction> actions, boolean waiting) {
        this.actions = actions;
        this.waiting = waiting;
    }

    public List<PeopleAction> actions() {
        return actions;
    }

    public boolean waiting() {
        return waiting;
    }

    public Stream<String> labels() {
        return Stream.concat(waiting ? Stream.of("Waiting...") : Stream.empty(),
                actions.stream().map(PeopleAction::title));
    }

    @Override
    public String toString() {
        return name() + ": кнопки " + actions + (waiting ? ", с плашкой ожидания" : ", без плашки ожидания");
    }
}
