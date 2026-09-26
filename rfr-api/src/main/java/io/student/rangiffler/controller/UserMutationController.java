package io.student.rangiffler.controller;

import io.student.rangiffler.model.types.FriendshipInput;
import io.student.rangiffler.model.types.User;
import io.student.rangiffler.model.types.UserInput;
import io.student.rangiffler.service.api.UserService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@PreAuthorize("isAuthenticated()")
public class UserMutationController {

  private final UserService userService;

  public UserMutationController(UserService userService) {
    this.userService = userService;
  }

  @MutationMapping
  public User user(@AuthenticationPrincipal Jwt principal,
                   @Argument("input") UserInput input) {
    String username = principal.getClaim("sub");
    return userService.updateUser(username, input);
  }

  @MutationMapping
  public User friendship(@AuthenticationPrincipal Jwt principal,
                         @Argument("input") FriendshipInput input) {
    String username = principal.getClaim("sub");
    return switch (input.getAction()) {
      case ADD -> userService.addFriend(username, UUID.fromString(input.getUser()));
      case ACCEPT -> userService.acceptInvitation(username, UUID.fromString(input.getUser()));
      case REJECT -> userService.declineInvitation(username, UUID.fromString(input.getUser()));
      case DELETE -> userService.removeFriend(username, UUID.fromString(input.getUser()));
    };
  }
}
