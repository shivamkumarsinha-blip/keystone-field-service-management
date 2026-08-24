package com.zidio.keystone.security;

import com.zidio.keystone.entity.User;
import com.zidio.keystone.exception.ForbiddenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Helper for pulling the authenticated User off the SecurityContext inside services. */
public final class SecurityUtils {

    private SecurityUtils() {}

    public static User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails cud)) {
            throw new ForbiddenException("No authenticated user in context");
        }
        return cud.getUser();
    }
}
