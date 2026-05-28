package com.socialmediablog.platform.services.user.infrastructure.adapter;

import com.socialmediablog.platform.services.user.domain.aggregate.UserMediaAsset;
import com.socialmediablog.platform.services.user.domain.repository.UserMediaAssetRepository;
import com.socialmediablog.platform.services.user.domain.vo.UserMediaAssetId;
import com.socialmediablog.platform.services.user.infrastructure.entity.JpaUserMediaAssetEntity;
import com.socialmediablog.platform.services.user.infrastructure.persistence.SpringDataJpaUserMediaAssetRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class JpaUserMediaAssetRepositoryAdapter implements UserMediaAssetRepository {

    private final SpringDataJpaUserMediaAssetRepository repository;

    public JpaUserMediaAssetRepositoryAdapter(SpringDataJpaUserMediaAssetRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<UserMediaAsset> findById(UserMediaAssetId id) {
        return repository.findById(id.value()).map(JpaUserMediaAssetEntity::toDomain);
    }

    @Override
    public List<UserMediaAsset> findByUserId(UUID userId) {
        return repository.findByUserId(userId).stream()
                .map(JpaUserMediaAssetEntity::toDomain)
                .toList();
    }

    @Override
    public UserMediaAsset save(UserMediaAsset mediaAsset) {
        return repository.save(JpaUserMediaAssetEntity.fromDomain(mediaAsset)).toDomain();
    }
}
