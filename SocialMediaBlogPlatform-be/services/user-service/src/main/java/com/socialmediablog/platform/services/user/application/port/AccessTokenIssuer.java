package com.socialmediablog.platform.services.user.application.port;

import com.socialmediablog.platform.services.user.application.IssuedToken;
import com.socialmediablog.platform.services.user.domain.User;

public interface AccessTokenIssuer {

    IssuedToken issue(User user);
}
