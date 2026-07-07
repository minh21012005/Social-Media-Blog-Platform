package com.socialmediablog.platform.services.follower.application.port.in;

import java.util.List;
import java.util.UUID;

public interface ListMutedUserIdsUseCase {
    List<UUID> listMutedIds(UUID muterId);
}
