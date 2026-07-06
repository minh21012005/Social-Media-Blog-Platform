package com.socialmediablog.platform.services.interaction.application.port.in;

import com.socialmediablog.platform.services.interaction.application.command.ListMyBookmarksQuery;
import java.util.List;
import java.util.UUID;

public interface ListMyBookmarksUseCase {
    List<UUID> execute(ListMyBookmarksQuery query);
}
