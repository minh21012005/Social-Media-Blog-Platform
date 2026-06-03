package com.socialmediablog.platform.services.interaction.application.port.in;

import com.socialmediablog.platform.services.interaction.application.command.CountArticleLikesQuery;

public interface CountArticleLikesUseCase {
    long execute(CountArticleLikesQuery query);
}
