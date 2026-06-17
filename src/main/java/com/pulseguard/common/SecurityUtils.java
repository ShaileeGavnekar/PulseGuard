package com.pulseguard.common;

import com.pulseguard.common.exception.NotFoundException;
import com.pulseguard.user.User;
import com.pulseguard.user.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Convenience helper for resolving the authenticated {@link User} from the security context.
 */
@Component
public class SecurityUtils {

    private final UserRepository userRepository;

    public SecurityUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User currentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = (principal instanceof UserDetails ud) ? ud.getUsername() : principal.toString();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));
    }
}
