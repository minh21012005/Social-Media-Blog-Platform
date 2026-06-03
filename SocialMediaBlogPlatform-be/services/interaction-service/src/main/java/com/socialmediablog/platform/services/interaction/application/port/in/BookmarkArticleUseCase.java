package com.socialmediablog.platform.services.interaction.application.port.in;

import com.socialmediablog.platform.services.interaction.application.command.BookmarkArticleCommand;

public interface BookmarkArticleUseCase {
    void execute(BookmarkArticleCommand command);
}
