package io.student.rangiffler.test.people;

import com.microsoft.playwright.Page;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.jupiter.annotation.User;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UiTest
@DisplayName("Вкладка Friends")
class FriendsTabTest {
    @Test
    @DisplayName("Показывает друзей и не показывает пользователей с приглашениями")
    void listsOnlyFriends(Page page,
                          @User(friends = 1, incomeInvitations = 1, outcomeInvitations = 1) TestUser user) {
        PeopleLogin.open(page, user)
                .table()
                .checkThatPeople(List.of(user.friends().getFirst().username()));
    }

    @Test
    @DisplayName("У друга единственная кнопка Remove")
    void friendOffersOnlyRemove(Page page, @User(friends = 1) TestUser user) {
        PeopleLogin.open(page, user)
                .table()
                .row(user.friends().getFirst().username())
                .checkThatActions("Remove");
    }

    @Test
    @DisplayName("Без друзей показывает пустое состояние")
    void showsEmptyStateWithoutFriends(Page page, @User TestUser user) {
        PeopleLogin.open(page, user)
                .table()
                .checkThatEmpty();
    }
}
