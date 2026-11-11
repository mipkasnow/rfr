package io.student.rangiffler.test.travels;

import com.microsoft.playwright.Page;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.network.AwaitedGqlOperation;
import io.student.rangiffler.network.GqlStubs;
import io.student.rangiffler.ui.page.MyTravelsPage;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UiTest
@DisplayName("Подмена лайков в GetFeed")
class FeedLikesPatchTest {
    private static final String PATCHED_PHOTO = "Амстердам!";
    private static final String NEIGHBOR_PHOTO = "Париж!";
    private static final String NEIGHBOR_LIKES = "2";
    private static final String LIKES_PATH =
            "$.data.feed.photos.edges[?(@.node.description == '" + PATCHED_PHOTO + "')].node.likes.total";

    @Test
    @DisplayName("shouldPatchBody меняет лайки одной карточки и оставляет соседнюю")
    void patchesLikesOfOneCardAndLeavesNeighbor(Page page, GqlStubs stubs, @User TestUser user) {
        int patchedLikes = randomLikes();
        stubs.shouldPatchBody("GetFeed", LIKES_PATH, patchedLikes);

        MyTravelsPage travels = TravelsLogin.open(page, user);
        new AwaitedGqlOperation(page, "GetFeed", travels::reload).await();

        travels.photo(PATCHED_PHOTO).checkThatLikes(String.valueOf(patchedLikes));
        travels.photo(NEIGHBOR_PHOTO).checkThatLikes(NEIGHBOR_LIKES);
    }

    private static int randomLikes() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int likes;
        do {
            likes = random.nextInt(11, 100);
        } while (likes % 10 == 0 || likes == Integer.parseInt(NEIGHBOR_LIKES));
        return likes;
    }
}
