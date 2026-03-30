package com.authServer.shared.tools;

import com.authServer.models.hq.admin.AdminEntity;
import com.authServer.models.hq.admin.AdminRepository;
import com.authServer.models.hq.client.ClientEntity;
import com.authServer.models.hq.client.ClientRepository;
import com.authServer.shared.models.baseUser.UserRoles;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthUserUtil {

    private final ClientRepository clientRepository;
    private final AdminRepository adminRepository;

    public AuthUserUtil(ClientRepository clientRepository, AdminRepository adminRepository) {
        this.clientRepository = clientRepository;
        this.adminRepository = adminRepository;
    }

    /* ------------------------------------------------------------------
     * Public helpers
     * ------------------------------------------------------------------ */

    public Optional<AdminEntity> getAuthUserAdminEntity() {
        return getAuthUsername()
                .map(adminRepository::findByUsername);
    }

    public Optional<ClientEntity> getAuthUserClientEntity() {
        return getAuthUsername()
                .flatMap(clientRepository::findByUsername);
    }

    public boolean isAuthUserClient() {
        return hasRole(UserRoles.CLIENT);
    }

    public boolean isAuthUserAdmin() {
        return hasRole(UserRoles.ADMIN);
    }

    public Optional<String> getAuthUsername() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        // Support both String principals (from our JWT filter) and UserDetails (from Login)
        Object principal = authentication.getPrincipal();
        if (principal instanceof String) {
            return Optional.of((String) principal);
        } else if (principal instanceof UserDetails) {
            return Optional.of(((UserDetails) principal).getUsername());
        }

        return Optional.ofNullable(authentication.getName());
    }

    /* ------------------------------------------------------------------
     * Private helpers
     * ------------------------------------------------------------------ */

    private boolean hasRole(UserRoles role) {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        // Check authorities directly from the SecurityContext (populated by JWT claims)
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role.getAuthority()));
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}