package com.authServer.configuration.filter;

import com.authServer.constant.ApplicationConstants;
import com.authServer.shared.tools.ErrorHTTPRes;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class JWTTokenValidatorFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;

    public JWTTokenValidatorFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
       String jwt = request.getHeader(ApplicationConstants.TK_HEADER);
       if (jwt != null && jwt.startsWith("Bearer ")){
           jwt = jwt.substring("Bearer ".length());
       }
       if (jwt != null){
           try{
               Claims claims = jwtTokenService.extractClaims(jwt);
               String username = String.valueOf(claims.get("username"));

               // Extract authorities from JWT claims
               Object authoritiesClaim = claims.get("authorities");
               List<GrantedAuthority> authorities = new ArrayList<>();

               if (authoritiesClaim instanceof List) {
                   for (Object authorityObj : (List<?>) authoritiesClaim) {
                       if (authorityObj instanceof Map) {
                           // Jackson deserializes SimpleGrantedAuthority as {"authority": "ROLE_ADMIN"}
                           String authority = (String) ((Map<?, ?>) authorityObj).get("authority");
                           if (authority != null) {
                               authorities.add(new SimpleGrantedAuthority(authority));
                           }
                       } else if (authorityObj instanceof String) {
                           authorities.add(new SimpleGrantedAuthority((String) authorityObj));
                       }
                   }
               }

               // Construct authentication token using the username as the principal
               Authentication authentication = new UsernamePasswordAuthenticationToken(
                       username,
                       null,
                       authorities
               );
               SecurityContextHolder.getContext().setAuthentication(authentication);

           } catch (Exception exception) {
               SecurityContextHolder.clearContext();
               response.resetBuffer();
               response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
               response.setContentType("application/json");
               ErrorHTTPRes res = ErrorHTTPRes.builder()
                       .timestamp(LocalDateTime.now().toString())
                       .status(HttpStatus.UNAUTHORIZED.value())
                       .error("Unauthorized")
                       .message("Invalid Token")
                       .path(request.getRequestURI())
                       .build();
               ObjectMapper objectMapper = new ObjectMapper();
               objectMapper.writeValue(response.getOutputStream(), res);
               return;
           }
       }
       filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getServletPath().startsWith("/login");
    }
}