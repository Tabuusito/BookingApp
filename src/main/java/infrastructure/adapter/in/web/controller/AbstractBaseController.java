package infrastructure.adapter.in.web.controller;

import infrastructure.adapter.in.web.security.RequesterContext;
import infrastructure.adapter.in.web.security.SpringSecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

// Esta es una clase base abstracta, no un componente de Spring.
public abstract class AbstractBaseController {

    protected RequesterContext createRequesterContext(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return new RequesterContext(Optional.empty(), Collections.emptySet());
        }

        Optional<Long> userId = Optional.empty();
        if (authentication.getPrincipal() instanceof SpringSecurityUser) {
            userId = Optional.of(((SpringSecurityUser) authentication.getPrincipal()).getId());
        }

        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return new RequesterContext(userId, roles);
    }
}