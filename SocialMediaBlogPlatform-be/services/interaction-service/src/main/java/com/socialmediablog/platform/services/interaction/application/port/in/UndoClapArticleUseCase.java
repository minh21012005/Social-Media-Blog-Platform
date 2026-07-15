package com.socialmediablog.platform.services.interaction.application.port.in;

import com.socialmediablog.platform.services.interaction.application.command.UndoClapArticleCommand;

public interface UndoClapArticleUseCase {

    long execute(UndoClapArticleCommand command);
}
