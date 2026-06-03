package com.socialmediablog.platform.services.interaction.application.port.in;

import com.socialmediablog.platform.services.interaction.application.command.LikeArticleCommand;

public interface LikeArticleUseCase {
    void execute(LikeArticleCommand command);
}
