package io.student.rangiffler.ui.page;

public enum PeopleTab {
    FRIENDS("Friends"),
    ALL_PEOPLE("All People"),
    OUTCOME_INVITATIONS("Outcome invitations"),
    INCOME_INVITATIONS("Income invitations");

    private final String title;

    PeopleTab(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }
}
