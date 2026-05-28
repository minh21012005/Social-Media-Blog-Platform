package com.socialmediablog.platform.services.article.application.usecase;

import com.socialmediablog.platform.services.article.application.command.GetServiceStatusCommand;
import com.socialmediablog.platform.services.article.application.port.in.GetServiceStatusUseCase;
import com.socialmediablog.platform.services.article.application.result.ServiceStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArticleApplicationService implements GetServiceStatusUseCase {

    @Override
    @Transactional(readOnly = true)
    public ServiceStatus execute(GetServiceStatusCommand command) {
        return new ServiceStatus("article-service", "articles", command.currentUserId());
    }
}
