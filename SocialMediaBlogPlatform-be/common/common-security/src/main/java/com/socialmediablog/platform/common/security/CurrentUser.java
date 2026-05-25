package com.socialmediablog.platform.common.security;

import java.util.Set;

public record CurrentUser(String id, String username, Set<String> roles) {

    public CurrentUser {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
