package com.socialmediablog.platform.services.follower.domain.repository;

import com.socialmediablog.platform.services.follower.domain.aggregate.Mute;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MuteRepository {
    Optional<Mute> findByMuterIdAndMutedUserId(UUID muterId, UUID mutedUserId);
    List<Mute> findByMuterId(UUID muterId, int page, int size);
    long countByMuterId(UUID muterId);
    List<UUID> findMutedUserIdsByMuterId(UUID muterId);
    Mute save(Mute mute);
    void delete(Mute mute);
}
