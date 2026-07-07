package com.socialmediablog.platform.services.follower.infrastructure.adapter;

import com.socialmediablog.platform.services.follower.domain.aggregate.Mute;
import com.socialmediablog.platform.services.follower.domain.repository.MuteRepository;
import com.socialmediablog.platform.services.follower.infrastructure.entity.JpaMuteEntity;
import com.socialmediablog.platform.services.follower.infrastructure.persistence.SpringDataJpaMuteRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
public class JpaMuteRepositoryAdapter implements MuteRepository {

    private final SpringDataJpaMuteRepository repository;

    public JpaMuteRepositoryAdapter(SpringDataJpaMuteRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Mute> findByMuterIdAndMutedUserId(UUID muterId, UUID mutedUserId) {
        return repository.findByMuterIdAndMutedUserId(muterId, mutedUserId).map(JpaMuteEntity::toDomain);
    }

    @Override
    public List<Mute> findByMuterId(UUID muterId, int page, int size) {
        return repository.findByMuterId(muterId, PageRequest.of(page, size)).stream().map(JpaMuteEntity::toDomain).toList();
    }

    @Override
    public long countByMuterId(UUID muterId) {
        return repository.countByMuterId(muterId);
    }

    @Override
    public List<UUID> findMutedUserIdsByMuterId(UUID muterId) {
        return repository.findMutedUserIdsByMuterId(muterId);
    }

    @Override
    public Mute save(Mute mute) {
        return repository.save(JpaMuteEntity.fromDomain(mute)).toDomain();
    }

    @Override
    public void delete(Mute mute) {
        repository.delete(JpaMuteEntity.fromDomain(mute));
    }
}
