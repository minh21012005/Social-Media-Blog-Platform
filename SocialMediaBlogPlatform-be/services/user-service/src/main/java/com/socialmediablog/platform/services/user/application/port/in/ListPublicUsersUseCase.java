package com.socialmediablog.platform.services.user.application.port.in;

import com.socialmediablog.platform.services.user.application.result.PublicUserProfile;
import java.util.List;
import java.util.UUID;

public interface ListPublicUsersUseCase {

    List<PublicUserProfile> executeBatch(List<UUID> userIds);

    List<PublicUserProfile> searchUsers(String query);
}
