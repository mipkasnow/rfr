package io.student.rangiffler.controller;

import io.student.rangiffler.model.types.Feed;
import io.student.rangiffler.model.types.Likes;
import io.student.rangiffler.model.types.Photo;
import io.student.rangiffler.model.types.Stat;
import io.student.rangiffler.service.mock.FeedMockData;
import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Objects;

@Controller
@PreAuthorize("hasAuthority('read')")
public class FeedMockQueryController {

  private static final int DEFAULT_PAGE = 0;
  private static final int DEFAULT_SIZE = 12;

  private final FeedMockData feedMockData;

  public FeedMockQueryController(FeedMockData feedMockData) {
    this.feedMockData = feedMockData;
  }

  @SchemaMapping(typeName = "Feed", field = "stat")
  public List<Stat> stat(Feed feed) {
    return feedMockData.stat(feed.getWithFriends());
  }

  @SchemaMapping(typeName = "Photo", field = "likes")
  public Likes likes(Photo photo) {
    return photo.getLikes();
  }

  @SchemaMapping(typeName = "Feed", field = "photos")
  public Page<Photo> photos(Feed feed,
                            @Argument("page") @Nullable Integer page,
                            @Argument("size") @Nullable Integer size,
                            @Argument("country") @Nullable String country) {
    PageRequest pageRequest = PageRequest.of(
        Objects.requireNonNullElse(page, DEFAULT_PAGE),
        Objects.requireNonNullElse(size, DEFAULT_SIZE)
    );
    List<Photo> photos = feedMockData.photos(feed.getWithFriends(), country);
    int from = (int) Math.min(pageRequest.getOffset(), photos.size());
    int to = Math.min(from + pageRequest.getPageSize(), photos.size());
    return new PageImpl<>(photos.subList(from, to), pageRequest, photos.size());
  }

  @QueryMapping
  public Feed feed(@AuthenticationPrincipal Jwt principal,
                   @Argument("withFriends") boolean withFriends) {
    return new Feed(
        principal.getClaim("sub"),
        withFriends,
        null,
        null
    );
  }
}
