package io.student.rangiffler.data.repository;

import io.student.rangiffler.data.entity.FriendshipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface FriendshipRepository extends JpaRepository<FriendshipEntity, UUID> {

  @Query("select f from FriendshipEntity f " +
      "where (f.requester.id = :first and f.addressee.id = :second) " +
      "   or (f.requester.id = :second and f.addressee.id = :first)")
  Optional<FriendshipEntity> findPair(@Param("first") UUID first, @Param("second") UUID second);
}
