package com.socialmediablog.platform.services.interaction.application.port.in;

import com.socialmediablog.platform.services.interaction.application.command.UnlikeArticleCommand;

public interface UnlikeArticleUseCase {
    void execute(UnlikeArticleCommand command);
}
