package io.student.rangiffler.data.projection;

import io.student.rangiffler.data.entity.FriendshipStatus;

import java.util.UUID;

public record UserWithStatus(
    UUID id,
    String username,
    String firstname,
    String lastName,
    byte[] avatar,
    String countryCode,
    String countryName,
    byte[] countryFlag,
    FriendshipStatus friendshipStatus,
    Boolean isRequester
) {
}
