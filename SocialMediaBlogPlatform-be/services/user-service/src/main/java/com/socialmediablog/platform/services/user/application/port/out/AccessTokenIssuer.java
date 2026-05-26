package com.socialmediablog.platform.services.user.application.port.out;

import com.socialmediablog.platform.services.user.application.result.IssuedToken;
import com.socialmediablog.platform.services.user.domain.aggregate.User;

public interface AccessTokenIssuer {

    IssuedToken issue(User user);
}
