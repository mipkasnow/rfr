package io.student.rangiffler.service.impl;

import io.student.rangiffler.data.entity.CountryEntity;
import io.student.rangiffler.data.entity.FriendshipEntity;
import io.student.rangiffler.data.entity.FriendshipStatus;
import io.student.rangiffler.data.entity.UserEntity;
import io.student.rangiffler.data.projection.UserWithStatus;
import io.student.rangiffler.data.repository.CountryRepository;
import io.student.rangiffler.data.repository.FriendshipRepository;
import io.student.rangiffler.data.repository.UserRepository;
import io.student.rangiffler.exception.FriendshipActionException;
import io.student.rangiffler.exception.ResourceNotFoundException;
import io.student.rangiffler.model.types.Country;
import io.student.rangiffler.model.types.FriendStatus;
import io.student.rangiffler.model.types.Stat;
import io.student.rangiffler.model.types.User;
import io.student.rangiffler.model.types.UserInput;
import io.student.rangiffler.service.api.UserService;
import io.student.rangiffler.util.BytesAsString;
import io.student.rangiffler.util.StringAsBytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final CountryRepository countryRepository;
  private final FriendshipRepository friendshipRepository;

  @Autowired
  public UserServiceImpl(UserRepository userRepository,
                         CountryRepository countryRepository,
                         FriendshipRepository friendshipRepository) {
    this.userRepository = userRepository;
    this.countryRepository = countryRepository;
    this.friendshipRepository = friendshipRepository;
  }

  @Override
  @Transactional
  public User createNewUserIfNotPresent(String username) {
    UserEntity userEntity = userRepository.findByUsername(username)
        .orElseGet(() -> {
          UserEntity newUser = new UserEntity();
          newUser.setUsername(username);
          newUser.setCountry(countryRepository.findByCode("ru").orElseThrow(() -> new ResourceNotFoundException(
              "Country not found by code: ru"
          )));
          return userRepository.save(newUser);
        });
    return toUser(userEntity, null);
  }

  @Override
  @Transactional(readOnly = true)
  public User currentUser(String username) {
    UserEntity userEntity = getRequiredUser(username);
    return toUser(userEntity, null);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<User> allUsers(String username, Pageable pageable, String searchQuery) {
    UUID me = getRequiredUser(username).getId();
    return (hasText(searchQuery)
        ? userRepository.findAllUsersWithFriendshipStatus(me, searchQuery, pageable)
        : userRepository.findAllUsersWithFriendshipStatus(me, pageable))
        .map(this::toUserFromProjection);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<User> friends(String username, Pageable pageable, String searchQuery) {
    UUID me = getRequiredUser(username).getId();
    return (hasText(searchQuery)
        ? userRepository.findFriends(me, searchQuery, pageable)
        : userRepository.findFriends(me, pageable))
        .map(this::toUserFromProjection);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<User> incomeInvitations(String username, Pageable pageable, String searchQuery) {
    UUID me = getRequiredUser(username).getId();
    return (hasText(searchQuery)
        ? userRepository.findIncomeInvitations(me, searchQuery, pageable)
        : userRepository.findIncomeInvitations(me, pageable))
        .map(this::toUserFromProjection);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<User> outcomeInvitations(String username, Pageable pageable, String searchQuery) {
    UUID me = getRequiredUser(username).getId();
    return (hasText(searchQuery)
        ? userRepository.findOutcomeInvitations(me, searchQuery, pageable)
        : userRepository.findOutcomeInvitations(me, pageable))
        .map(this::toUserFromProjection);
  }

  @Override
  @Transactional
  public User updateUser(String username, UserInput input) {
    UserEntity userEntity = getRequiredUser(username);

    if (input.getFirstname() != null) {
      userEntity.setFirstname(input.getFirstname());
    }
    if (input.getSurname() != null) {
      userEntity.setLastName(input.getSurname());
    }
    if (input.getAvatar() != null) {
      userEntity.setAvatar(new StringAsBytes(input.getAvatar()).bytes());
    }
    if (input.getLocation() != null) {
      CountryEntity country = countryRepository.findByCode(input.getLocation().getCode())
          .orElseThrow(() -> new ResourceNotFoundException(
              String.format("Country not found by code: %s", input.getLocation().getCode())));
      userEntity.setCountry(country);
    }

    UserEntity saved = userRepository.save(userEntity);
    return toUser(saved, null);
  }

  @Override
  @Transactional
  public User addFriend(String username, UUID friendId) {
    UserEntity currentUser = getRequiredUser(username);
    UserEntity friend = getTargetUser(currentUser, friendId);

    friendshipRepository.findPair(currentUser.getId(), friend.getId()).ifPresent(existing -> {
      throw new FriendshipActionException(existingPairMessage(existing, currentUser));
    });
    try {
      friendshipRepository.saveAndFlush(FriendshipEntity.request(currentUser, friend));
    } catch (DataIntegrityViolationException e) {
      throw new FriendshipActionException("Friendship with this user already exists, please reload");
    }

    return toUser(friend, FriendStatus.INVITATION_SENT);
  }

  @Override
  @Transactional
  public User acceptInvitation(String username, UUID friendId) {
    UserEntity currentUser = getRequiredUser(username);
    UserEntity inviteUser = getTargetUser(currentUser, friendId);

    friendshipRepository.findPair(currentUser.getId(), inviteUser.getId())
        .orElseThrow(() -> new FriendshipActionException(FriendshipEntity.NO_PENDING_INVITATION))
        .accept(currentUser);
    flushFriendshipChange();

    return toUser(inviteUser, FriendStatus.FRIEND);
  }

  @Override
  @Transactional
  public User declineInvitation(String username, UUID friendId) {
    UserEntity currentUser = getRequiredUser(username);
    UserEntity friendToDecline = getTargetUser(currentUser, friendId);

    FriendshipEntity invitation = friendshipRepository.findPair(currentUser.getId(), friendToDecline.getId())
        .orElseThrow(() -> new FriendshipActionException(FriendshipEntity.NO_PENDING_INVITATION));
    invitation.assertCanDecline(currentUser);
    friendshipRepository.delete(invitation);
    flushFriendshipChange();

    return toUser(friendToDecline, FriendStatus.NOT_FRIEND);
  }

  @Override
  @Transactional
  public User removeFriend(String username, UUID friendId) {
    UserEntity currentUser = getRequiredUser(username);
    UserEntity friend = getTargetUser(currentUser, friendId);

    FriendshipEntity friendship = friendshipRepository.findPair(currentUser.getId(), friend.getId())
        .orElseThrow(() -> new FriendshipActionException("No friendship or invitation with this user"));
    friendshipRepository.delete(friendship);
    flushFriendshipChange();

    return toUser(friend, FriendStatus.NOT_FRIEND);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Stat> stat(String username, boolean withFriends) {
    return List.of();
  }

  private User toUser(UserEntity entity, FriendStatus friendStatus) {
    return User.newBuilder()
        .id(entity.getId().toString())
        .username(entity.getUsername())
        .firstname(entity.getFirstname())
        .surname(entity.getLastName())
        .avatar(new BytesAsString(entity.getAvatar()).string())
        .friendStatus(friendStatus)
        .location(entity.getCountry() != null ? toCountry(entity.getCountry()) : null)
        .build();
  }

  private Country toCountry(CountryEntity entity) {
    return Country.newBuilder()
        .code(entity.getCode())
        .name(entity.getName())
        .flag(new BytesAsString(entity.getFlag()).string())
        .build();
  }

  private User toUserFromProjection(UserWithStatus projection) {
    FriendStatus friendStatus = calculateFriendStatus(
        projection.friendshipStatus(),
        projection.isRequester()
    );

    return User.newBuilder()
        .id(projection.id().toString())
        .username(projection.username())
        .firstname(projection.firstname())
        .surname(projection.lastName())
        .avatar(new BytesAsString(projection.avatar()).string())
        .friendStatus(friendStatus)
        .location(Country.newBuilder()
            .code(projection.countryCode())
            .name(projection.countryName())
            .flag(new BytesAsString(projection.countryFlag()).string())
            .build())
        .build();
  }

  private FriendStatus calculateFriendStatus(FriendshipStatus status, Boolean isRequester) {
    if (status == FriendshipStatus.ACCEPTED) {
      return FriendStatus.FRIEND;
    }
    if (status == FriendshipStatus.PENDING) {
      return Boolean.TRUE.equals(isRequester)
          ? FriendStatus.INVITATION_SENT
          : FriendStatus.INVITATION_RECEIVED;
    }
    return FriendStatus.NOT_FRIEND;
  }

  private UserEntity getRequiredUser(String username) {
    return userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException(
            String.format("User not found by username: %s", username)
        ));
  }

  private UserEntity getTargetUser(UserEntity currentUser, UUID targetId) {
    if (currentUser.getId().equals(targetId)) {
      throw new FriendshipActionException("Cannot perform friendship action on yourself");
    }
    return getRequiredUser(targetId);
  }

  private void flushFriendshipChange() {
    try {
      friendshipRepository.flush();
    } catch (OptimisticLockingFailureException e) {
      throw new FriendshipActionException("Friendship was changed by another action, please reload");
    }
  }

  private static String existingPairMessage(FriendshipEntity existing, UserEntity currentUser) {
    if (existing.isAccepted()) {
      return "Already friends";
    }
    return existing.isRequester(currentUser)
        ? "Invitation already sent"
        : "There is already an incoming invitation from this user";
  }

  private static boolean hasText(String value) {
    return value != null && !value.isBlank();
  }

  private UserEntity getRequiredUser(UUID userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException(
            String.format("User not found by id: %s", userId)
        ));
  }
}
