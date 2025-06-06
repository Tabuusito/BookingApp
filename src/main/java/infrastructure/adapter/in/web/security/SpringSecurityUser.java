package infrastructure.adapter.in.web.security;

import domain.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class SpringSecurityUser implements UserDetails {

    private final User user;

    public SpringSecurityUser(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getAuthority()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        // Mapear desde entidad de dominio si tuviese un campo para esto
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Idem
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // Idem
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getActive();
    }
}
