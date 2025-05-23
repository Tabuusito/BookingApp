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
        // Aquí es donde se mapearían los roles del dominio a GrantedAuthority.
        // Por simplicidad, un rol fijo, pero en un sistema real,
        // user.getRoles() si la entidad del dominio los tuviera.
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
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
