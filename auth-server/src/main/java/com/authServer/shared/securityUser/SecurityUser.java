package com.authServer.shared.securityUser;

import com.authServer.shared.models.baseUser.BaseUserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public class SecurityUser implements UserDetails{

    private final BaseUserEntity baseUser;

    public SecurityUser(BaseUserEntity baseUser){
        this.baseUser = baseUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return baseUser.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return baseUser.getPassword();
    }

    @Override
    public String getUsername() {
        return baseUser.getUsername();
    }

    public boolean getAccountNonExpired() {
        return baseUser.isAccountNonExpired();
    }

    public boolean getAccountNonLocked() {
        return baseUser.isAccountNonLocked();
    }

    public boolean getCredentialsNonExpired() {
        return baseUser.isCredentialsNonExpired();
    }

    public boolean getEnabled() {
        return baseUser.isEnabled();
    }

    public BaseUserEntity getBaseUser() {
        return baseUser;
    }
}