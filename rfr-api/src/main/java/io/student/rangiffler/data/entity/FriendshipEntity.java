package io.student.rangiffler.data.entity;

import io.student.rangiffler.exception.FriendshipActionException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

@Getter
@Entity
@Table(name = "friendship")
public class FriendshipEntity {

  public static final String NO_PENDING_INVITATION = "No pending invitation from this user";

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", nullable = false, columnDefinition = "BINARY(16)")
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "requester_id", nullable = false, updatable = false)
  private UserEntity requester;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "addressee_id", nullable = false, updatable = false)
  private UserEntity addressee;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private FriendshipStatus status;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "responded_at")
  private LocalDateTime respondedAt;

  @Version
  @Column(name = "version", nullable = false)
  private long version;

  protected FriendshipEntity() {
  }

  public static FriendshipEntity request(UserEntity requester, UserEntity addressee) {
    FriendshipEntity friendship = new FriendshipEntity();
    friendship.requester = requester;
    friendship.addressee = addressee;
    friendship.status = FriendshipStatus.PENDING;
    friendship.createdAt = now();
    return friendship;
  }

  public void accept(UserEntity actor) {
    assertPendingFor(actor);
    status = FriendshipStatus.ACCEPTED;
    respondedAt = now();
  }

  public void assertCanDecline(UserEntity actor) {
    assertPendingFor(actor);
  }

  public boolean isRequester(UserEntity user) {
    return requester.getId().equals(user.getId());
  }

  public boolean isAccepted() {
    return status == FriendshipStatus.ACCEPTED;
  }

  private void assertPendingFor(UserEntity actor) {
    if (status != FriendshipStatus.PENDING || !addressee.getId().equals(actor.getId())) {
      throw new FriendshipActionException(NO_PENDING_INVITATION);
    }
  }

  private static LocalDateTime now() {
    return LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
    Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    FriendshipEntity that = (FriendshipEntity) o;
    return getId() != null && Objects.equals(getId(), that.getId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
  }
}
