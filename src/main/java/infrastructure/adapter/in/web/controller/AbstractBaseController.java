package infrastructure.adapter.in.web.controller;

import infrastructure.adapter.in.web.security.RequesterContext;
import infrastructure.adapter.in.web.security.SpringSecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class AbstractBaseController {

    public RequesterContext createRequesterContext(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return new RequesterContext(Optional.empty(), Collections.emptySet());
        }

        Optional<UUID> userUuid = Optional.empty();
        if (authentication.getPrincipal() instanceof SpringSecurityUser springUser) {
            userUuid = Optional.of(springUser.getUuid());
        } else if (authentication.getPrincipal() instanceof String) {
            // A veces, para usuarios anónimos, el principal es solo un String "anonymousUser".
            // No hacemos nada, userUuid ya es Optional.empty().
        }

        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return new RequesterContext(userUuid, roles);
    }
}