package io.student.rangiffler.controller;

import graphql.schema.DataFetchingEnvironment;
import io.student.rangiffler.model.types.User;
import io.student.rangiffler.service.api.UserService;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

import java.util.Objects;

@Controller
@PreAuthorize("hasAuthority('read')")
public class UserQueryController {

  private static final int DEFAULT_PAGE = 0;
  private static final int DEFAULT_SIZE = 10;

  private final UserService userService;

  @Autowired
  public UserQueryController(UserService userService) {
    this.userService = userService;
  }

  @SchemaMapping(typeName = "User", field = "friends")
  public Page<User> friends(User user,
                            @Argument("page") @Nullable Integer page,
                            @Argument("size") @Nullable Integer size,
                            @Argument("searchQuery") @Nullable String searchQuery) {
    return userService.friends(
        user.getUsername(),
        pageRequest(page, size),
        searchQuery
    );
  }

  @SchemaMapping(typeName = "User", field = "incomeInvitations")
  public Page<User> incomeInvitations(User user,
                                      @Argument("page") @Nullable Integer page,
                                      @Argument("size") @Nullable Integer size,
                                      @Argument("searchQuery") @Nullable String searchQuery) {
    return userService.incomeInvitations(
        user.getUsername(),
        pageRequest(page, size),
        searchQuery
    );
  }

  @SchemaMapping(typeName = "User", field = "outcomeInvitations")
  public Page<User> outcomeInvitations(User user,
                                       @Argument("page") @Nullable Integer page,
                                       @Argument("size") @Nullable Integer size,
                                       @Argument("searchQuery") @Nullable String searchQuery) {
    return userService.outcomeInvitations(
        user.getUsername(),
        pageRequest(page, size),
        searchQuery
    );
  }

  @QueryMapping
  public User user(@AuthenticationPrincipal Jwt principal,
                   DataFetchingEnvironment env) {
    return userService.createNewUserIfNotPresent(principal.getClaim("sub"));
  }

  @QueryMapping
  public Page<User> users(@AuthenticationPrincipal Jwt principal,
                          @Argument("page") @Nullable Integer page,
                          @Argument("size") @Nullable Integer size,
                          @Argument("searchQuery") @Nullable String searchQuery) {
    return userService.allUsers(
        principal.getClaim("sub"),
        pageRequest(page, size),
        searchQuery
    );
  }

  private static PageRequest pageRequest(@Nullable Integer page, @Nullable Integer size) {
    return PageRequest.of(
        Objects.requireNonNullElse(page, DEFAULT_PAGE),
        Objects.requireNonNullElse(size, DEFAULT_SIZE)
    );
  }
}
