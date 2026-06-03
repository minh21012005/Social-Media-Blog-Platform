package com.socialmediablog.platform.services.interaction.application.port.in;

import com.socialmediablog.platform.services.interaction.application.command.IsArticleLikedQuery;

public interface IsArticleLikedUseCase {
    boolean execute(IsArticleLikedQuery query);
}
