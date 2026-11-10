package io.student.rangiffler.test.people;

import com.microsoft.playwright.Page;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.network.GqlStubs;
import io.student.rangiffler.network.HeldOperation;
import io.student.rangiffler.ui.page.PeoplePage;
import io.student.rangiffler.ui.page.PeopleTab;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UiTest
@DisplayName("Загрузка таблицы People")
class PeopleLoaderTest {
    @Test
    @DisplayName("All People показывает загрузчик, пока не пришёл ответ GetPeople")
    void allPeopleShowsLoaderUntilPeopleArrive(Page page, GqlStubs stubs, @User TestUser user) {
        PeoplePage people = PeopleLogin.open(page, user);
        HeldOperation held = stubs.hold("GetPeople");
        people.openTab(PeopleTab.ALL_PEOPLE)
                .table()
                .checkThatLoaderShown();
        held.release();
        people.table()
                .checkThatLoaderHidden();
    }
}
