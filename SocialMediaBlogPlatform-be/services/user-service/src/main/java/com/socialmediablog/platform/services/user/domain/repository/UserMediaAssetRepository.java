package com.socialmediablog.platform.services.user.domain.repository;

import com.socialmediablog.platform.services.user.domain.aggregate.UserMediaAsset;
import com.socialmediablog.platform.services.user.domain.vo.UserMediaAssetId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserMediaAssetRepository {

    Optional<UserMediaAsset> findById(UserMediaAssetId id);

    List<UserMediaAsset> findByUserId(UUID userId);

    UserMediaAsset save(UserMediaAsset mediaAsset);
}
