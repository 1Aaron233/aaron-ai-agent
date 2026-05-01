package com.aaron.aaronaiagent.security;

import com.aaron.aaronaiagent.rbac.model.RbacUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class SecurityUser implements UserDetails {

    private final RbacUser user;
    private final Set<GrantedAuthority> authorities;

    public SecurityUser(RbacUser user) {
        this.user = user;
        this.authorities = buildAuthorities(user);
    }

    private Set<GrantedAuthority> buildAuthorities(RbacUser user) {
        Set<GrantedAuthority> result = new LinkedHashSet<>();
        user.roles().forEach(role -> {
            result.add(new SimpleGrantedAuthority("ROLE_" + role.code()));
            role.permissions().forEach(permission -> result.add(new SimpleGrantedAuthority(permission.code())));
        });
        return result;
    }

    public Long getUserId() {
        return user.id();
    }

    public String getNickname() {
        return user.nickname();
    }

    public String getStatus() {
        return user.status();
    }

    public RbacUser getSourceUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.passwordHash();
    }

    @Override
    public String getUsername() {
        return user.username();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "ACTIVE".equalsIgnoreCase(user.status());
    }
}
