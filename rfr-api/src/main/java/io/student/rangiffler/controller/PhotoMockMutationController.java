package io.student.rangiffler.controller;

import io.student.rangiffler.exception.ResourceNotFoundException;
import io.student.rangiffler.model.types.Like;
import io.student.rangiffler.model.types.Likes;
import io.student.rangiffler.model.types.Photo;
import io.student.rangiffler.model.types.PhotoInput;
import io.student.rangiffler.service.api.CountryService;
import io.student.rangiffler.service.mock.FeedMockData;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Controller
@PreAuthorize("isAuthenticated()")
public class PhotoMockMutationController {

  private final CountryService countryService;
  private final FeedMockData feedMockData;

  public PhotoMockMutationController(CountryService countryService, FeedMockData feedMockData) {
    this.countryService = countryService;
    this.feedMockData = feedMockData;
  }

  @MutationMapping
  public Photo photo(@AuthenticationPrincipal Jwt principal,
                     @Argument("input") PhotoInput input) {
    Optional<Photo> existing = Optional.ofNullable(input.getId()).flatMap(feedMockData::findPhoto);
    if (input.getLike() != null) {
      Photo photo = existing.orElseThrow(() -> new ResourceNotFoundException(
          String.format("Photo not found by id: %s", input.getId())));
      List<Like> likes = new ArrayList<>(Objects.requireNonNullElse(photo.getLikes().getLikes(), List.of()));
      likes.add(Like.newBuilder()
          .user(input.getLike().getUser())
          .username(principal.getClaim("sub"))
          .creationDate(LocalDate.now())
          .build());
      photo.setLikes(new Likes(photo.getLikes().getTotal() + 1, likes));
      return photo;
    }

    if (input.getCountry() == null) {
      throw new IllegalArgumentException("Photo country is required");
    }
    String src = input.getSrc() != null ? input.getSrc() : existing.map(Photo::getSrc).orElse(null);
    if (src == null || src.isBlank()) {
      throw new IllegalArgumentException("Photo src is required");
    }
    return Photo.newBuilder()
        .id(input.getId() != null ? input.getId() : UUID.randomUUID().toString())
        .src(src)
        .description(Objects.requireNonNullElse(input.getDescription(), ""))
        .country(countryService.getByCode(input.getCountry().getCode()))
        .creationDate(LocalDate.now())
        .likes(existing.map(Photo::getLikes).orElseGet(() -> new Likes(0, List.of())))
        .isOwner(true)
        .build();
  }

  @MutationMapping
  public Boolean deletePhoto(@AuthenticationPrincipal Jwt principal, @Argument("id") UUID id) {
    return true;
  }
}
