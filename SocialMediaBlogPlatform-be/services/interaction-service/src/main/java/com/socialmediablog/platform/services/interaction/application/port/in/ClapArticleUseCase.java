package com.socialmediablog.platform.services.interaction.application.port.in;

import com.socialmediablog.platform.services.interaction.application.command.ClapArticleCommand;
import java.util.UUID;

public interface ClapArticleUseCase {

    long execute(ClapArticleCommand command);

    long getArticleClapCount(UUID articleId);

    boolean hasUserClappedArticle(UUID userId, UUID articleId);
}
