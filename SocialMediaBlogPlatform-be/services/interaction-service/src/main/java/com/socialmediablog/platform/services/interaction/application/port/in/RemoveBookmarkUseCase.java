package com.socialmediablog.platform.services.interaction.application.port.in;

import com.socialmediablog.platform.services.interaction.application.command.RemoveBookmarkCommand;

public interface RemoveBookmarkUseCase {
    void execute(RemoveBookmarkCommand command);
}
