package com.authServer.shared.securityUser;

import com.authServer.shared.models.baseUser.BaseUserEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SecurityUserServiceImpl implements UserDetailsService {

    private final BaseUserRepository baseUserRepository;

    public SecurityUserServiceImpl(BaseUserRepository baseUserRepository){
        this.baseUserRepository = baseUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        BaseUserEntity user = baseUserRepository.findByUsername(username)
                .orElseThrow(
                        () -> new UsernameNotFoundException("User not found")
                );
        return new SecurityUser(user);
    }

    public BaseUserEntity getBaseUser(String username) throws UsernameNotFoundException{
        BaseUserEntity user = baseUserRepository.findByUsername(username)
                .orElseThrow(
                        () -> new UsernameNotFoundException("User not found")
                );
        return user;
    }
}
