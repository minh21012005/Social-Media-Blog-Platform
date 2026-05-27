package com.socialmediablog.platform.services.user.application.port.out;

public interface RefreshTokenHasher {

    String hash(String refreshToken);
}
