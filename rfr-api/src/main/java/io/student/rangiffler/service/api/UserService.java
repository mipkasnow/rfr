package io.student.rangiffler.service.api;

import io.student.rangiffler.model.types.Stat;
import io.student.rangiffler.model.types.User;
import io.student.rangiffler.model.types.UserInput;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserService {

  User createNewUserIfNotPresent(String username);

  User currentUser(String username);

  Page<User> allUsers(String username, Pageable pageable, String searchQuery);

  Page<User> friends(String username, Pageable pageable, String searchQuery);

  Page<User> incomeInvitations(String username, Pageable pageable, String searchQuery);

  Page<User> outcomeInvitations(String username, Pageable pageable, String searchQuery);

  User updateUser(String username, UserInput input);

  User addFriend(String username, UUID friendId);

  User acceptInvitation(String username, UUID friendId);

  User declineInvitation(String username, UUID friendId);

  User removeFriend(String username, UUID friendId);

  List<Stat> stat(String username, boolean withFriends);
}
