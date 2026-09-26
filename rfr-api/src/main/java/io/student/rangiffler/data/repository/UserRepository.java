package io.student.rangiffler.data.repository;

import io.student.rangiffler.data.entity.UserEntity;
import io.student.rangiffler.data.projection.UserWithStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

  String SELECT_USER_WITH_STATUS =
      "select new io.student.rangiffler.data.projection.UserWithStatus(" +
          "u.id, u.username, u.firstname, u.lastName, u.avatar, c.code, c.name, c.flag, " +
          "f.status, " +
          "case when f.requester.id = :me then true when f.addressee.id = :me then false else null end) " +
          "from UserEntity u join u.country c ";
  String PAIR_WITH_ME =
      "FriendshipEntity f on (f.requester.id = :me and f.addressee = u) " +
          "or (f.addressee.id = :me and f.requester = u) ";
  String SEARCH =
      "and (lower(u.username) like lower(concat('%', :searchQuery, '%')) " +
          "or lower(u.firstname) like lower(concat('%', :searchQuery, '%')) " +
          "or lower(u.lastName) like lower(concat('%', :searchQuery, '%'))) ";
  String ORDER = "order by u.username asc";

  String ALL_USERS = SELECT_USER_WITH_STATUS + "left join " + PAIR_WITH_ME + "where u.id <> :me ";
  String FRIENDS = SELECT_USER_WITH_STATUS + "join " + PAIR_WITH_ME +
      "where f.status = io.student.rangiffler.data.entity.FriendshipStatus.ACCEPTED ";
  String INCOME_INVITATIONS = SELECT_USER_WITH_STATUS +
      "join FriendshipEntity f on f.requester = u and f.addressee.id = :me " +
      "where f.status = io.student.rangiffler.data.entity.FriendshipStatus.PENDING ";
  String OUTCOME_INVITATIONS = SELECT_USER_WITH_STATUS +
      "join FriendshipEntity f on f.addressee = u and f.requester.id = :me " +
      "where f.status = io.student.rangiffler.data.entity.FriendshipStatus.PENDING ";

  Optional<UserEntity> findByUsername(String username);

  @Query(ALL_USERS + ORDER)
  Page<UserWithStatus> findAllUsersWithFriendshipStatus(@Param("me") UUID me,
                                                        Pageable pageable);

  @Query(ALL_USERS + SEARCH + ORDER)
  Page<UserWithStatus> findAllUsersWithFriendshipStatus(@Param("me") UUID me,
                                                        @Param("searchQuery") String searchQuery,
                                                        Pageable pageable);

  @Query(FRIENDS + ORDER)
  Page<UserWithStatus> findFriends(@Param("me") UUID me,
                                   Pageable pageable);

  @Query(FRIENDS + SEARCH + ORDER)
  Page<UserWithStatus> findFriends(@Param("me") UUID me,
                                   @Param("searchQuery") String searchQuery,
                                   Pageable pageable);

  @Query(OUTCOME_INVITATIONS + ORDER)
  Page<UserWithStatus> findOutcomeInvitations(@Param("me") UUID me,
                                              Pageable pageable);

  @Query(OUTCOME_INVITATIONS + SEARCH + ORDER)
  Page<UserWithStatus> findOutcomeInvitations(@Param("me") UUID me,
                                              @Param("searchQuery") String searchQuery,
                                              Pageable pageable);

  @Query(INCOME_INVITATIONS + ORDER)
  Page<UserWithStatus> findIncomeInvitations(@Param("me") UUID me,
                                             Pageable pageable);

  @Query(INCOME_INVITATIONS + SEARCH + ORDER)
  Page<UserWithStatus> findIncomeInvitations(@Param("me") UUID me,
                                             @Param("searchQuery") String searchQuery,
                                             Pageable pageable);
}
