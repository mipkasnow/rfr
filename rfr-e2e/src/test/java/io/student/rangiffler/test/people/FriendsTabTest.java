package io.student.rangiffler.test.people;

import com.microsoft.playwright.Page;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.jupiter.annotation.meta.WebTest;
import io.student.rangiffler.ui.page.LandingPage;
import io.student.rangiffler.ui.page.PeoplePage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@WebTest
@DisplayName("Тесты дружбы")
public class FriendsTabTest {

    @Test
    @DisplayName("Пользователь с 1 входящей и 1 исходящей заявкой в друзья не имеет друзей на вкладке Friends")
    void friendsEmptyList(Page page, @User(incomeInvitations = 1, outcomeInvitations = 1) TestUser user) {
        friends(page, user).checkThatFriendsListIsEmpty();
    }

    @Test
    @DisplayName("Пользователь с 1 другом и 1 входящей заявкой видит друга и кнопку Remove на вкладке Friends")
    void friendsList(Page page, @User(friends = 1, incomeInvitations = 1) TestUser user) {
        String friend = user.friends().getFirst().username();
        friends(page, user)
                .checkThatPeople(List.of(friend))
                .checkThatRemoveButton(friend);
    }

    private PeoplePage friends(Page page, TestUser user) {
        return new LandingPage(page).open()
                .clickLogin()
                .fillLoginForm(user.username(), user.password())
                .clickSignIn()
                .clickPeople()
                .clickFriends();
    }
}
