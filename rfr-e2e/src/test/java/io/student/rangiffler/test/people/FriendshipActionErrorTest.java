package io.student.rangiffler.test.people;

import com.microsoft.playwright.Page;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.domain.FriendStatus;
import io.student.rangiffler.domain.Notification;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.network.GqlErrorType;
import io.student.rangiffler.network.GqlStubs;
import io.student.rangiffler.ui.page.PeoplePage;
import io.student.rangiffler.ui.page.PeopleTab;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UiTest
@DisplayName("Ошибка FriendshipAction в UI")
class FriendshipActionErrorTest {
    @Test
    @DisplayName("BAD_REQUEST на Add показывает текст сервера, статус NOT_FRIEND")
    void badRequestOnAddShowsServerText(Page page, GqlStubs stubs, @User TestUser alice, @User TestUser bob) {
        String serverText = randomServerText();
        stubs.error("FriendshipAction", GqlErrorType.BAD_REQUEST, serverText);

        PeoplePage people = PeopleLogin.open(page, alice)
                .openTab(PeopleTab.ALL_PEOPLE)
                .searchPeople(bob.username());
        people.table().row(bob.username()).clickAdd();

        people.checkThatNotification(serverText);
        people.table().row(bob.username()).checkThatStatus(FriendStatus.NOT_FRIEND);
    }

    @Test
    @DisplayName("INTERNAL_ERROR на Add показывает общий текст фронта, статус NOT_FRIEND")
    void internalErrorOnAddShowsFrontendFallback(Page page, GqlStubs stubs, @User TestUser alice, @User TestUser bob) {
        String serverText = randomServerText();
        stubs.error("FriendshipAction", GqlErrorType.INTERNAL_ERROR, serverText);

        PeoplePage people = PeopleLogin.open(page, alice)
                .openTab(PeopleTab.ALL_PEOPLE)
                .searchPeople(bob.username());
        people.table().row(bob.username()).clickAdd();

        people.checkThatNotification(Notification.CAN_NOT_SEND_INVITATION);
        people.checkThatNotificationAbsent(serverText);
        people.table().row(bob.username()).checkThatStatus(FriendStatus.NOT_FRIEND);
    }

    private static String randomServerText() {
        return "Friendship stub error " + UUID.randomUUID();
    }
}
