package io.student.rangiffler.test.travels;

import com.microsoft.playwright.Page;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.domain.Notification;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.network.AwaitedGqlOperation;
import io.student.rangiffler.network.GqlStubs;
import io.student.rangiffler.ui.page.MyTravelsPage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UiTest
@DisplayName("Удаление фото")
class PhotoDeletionTest {
    private static final String AMSTERDAM = "Амстердам!";

    @Test
    @Disabled("Не реализовано в rfr-api")
    @DisplayName("После удаления и перезагрузки в ленте на одно фото меньше")
    void deletedPhotoDisappearsAfterReload(Page page, GqlStubs stubs, @User TestUser user) {
        MyTravelsPage travels = TravelsLogin.open(page, user)
                .checkThatPhotoCards(4);
        stubs.shouldReturnBody("GetFeed", "gql/get-feed-without-amsterdam.json");
        travels.photo(AMSTERDAM)
                .clickDelete();
        travels.checkThatNotification(Notification.PHOTO_DELETED);
        new AwaitedGqlOperation(page, "GetFeed", travels::reload).await();
        travels.checkThatPhotoCards(3);
    }
}
