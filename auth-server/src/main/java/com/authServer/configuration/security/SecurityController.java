package com.authServer.configuration.security;

import com.authServer.configuration.filter.JwtTokenService;
import com.authServer.constant.ApplicationConstants;
import com.authServer.shared.models.baseUser.BaseUserEntity;
import com.authServer.shared.securityUser.SecurityUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class SecurityController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    public SecurityController(
            AuthenticationManager authenticationManager,
            JwtTokenService jwtTokenService
    ){
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping({"/login", "/login/"})
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginForm loginForm) {
        // 1. Authenticate the user
        Authentication authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(
                loginForm.getUsername(),
                loginForm.getPassword()
        );

        // This call triggers SecurityUserServiceImpl.loadUserByUsername()
        Authentication authenticationResponse = authenticationManager.authenticate(authenticationRequest);

        // 2. Extract user details
        SecurityUser securityUser = (SecurityUser) authenticationResponse.getPrincipal();
        BaseUserEntity user = securityUser.getBaseUser();

        // 3. Generate token
        String jwt = jwtTokenService.generateToken(user, securityUser.getAuthorities());

        // 4. Build response
        List<String> roles = securityUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        LoginResponseDTO response = LoginResponseDTO.builder()
                .token(jwt)
                .username(user.getUsername())
                .roles(roles)
                .build();

        return ResponseEntity.ok()
                .header(ApplicationConstants.TK_HEADER, jwt) // Maintain header for compatibility if needed
                .body(response);
    }

    @GetMapping("/test")
    public String testFilter(){
        return "test";
    }

}

