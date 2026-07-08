package com.socialmediablog.platform.services.interaction.application.port.in;

import com.socialmediablog.platform.services.interaction.application.command.BookmarkArticleCommand;
import com.socialmediablog.platform.services.interaction.application.result.BookmarkView;
import java.util.List;
import java.util.UUID;

public interface BookmarkArticleUseCase {
    void execute(BookmarkArticleCommand command);

    List<BookmarkView> listBookmarks(UUID userId);

    boolean isBookmarked(UUID userId, UUID articleId);
}
