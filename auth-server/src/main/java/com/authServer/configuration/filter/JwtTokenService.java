package com.authServer.configuration.filter;

import com.authServer.constant.ApplicationConstants;
import com.authServer.shared.models.baseUser.BaseUserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;

@Component
public class JwtTokenService {

    @Value("${" + ApplicationConstants.TK_ENV_KEY + "}")
    private String secretFromEnv;

    private SecretKey secretKey;

    @PostConstruct
    void init() {
        this.secretKey = Keys.hmacShaKeyFor(secretFromEnv.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(BaseUserEntity user,
                                Collection<? extends GrantedAuthority> authorities) {

        return Jwts.builder()
                .issuer(ApplicationConstants.ISSUER)
                .subject(ApplicationConstants.TK_SUBJECT)
                .claim("id", user.getId())
                .claim("username", user.getUsername())
                .claim("authorities", authorities)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86_400_000L)) // 24 h
                .signWith(secretKey)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}