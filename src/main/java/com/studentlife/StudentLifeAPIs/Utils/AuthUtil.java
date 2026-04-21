package com.studentlife.StudentLifeAPIs.Utils;

import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Jwt.JwtService;
import com.studentlife.StudentLifeAPIs.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.unauthorized;

@Component
@RequiredArgsConstructor
public class AuthUtil {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public Users getAuthenticatedUser() {
        Users currentUser = jwtService.getCurrentUser();

        if (currentUser == null) {
            throw unauthorized("Authentication required - no authenticated user found");
        }

        return currentUser;
    }

    public Long getUserIdFromPrincipal(java.security.Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Not authenticated.");
        }
        // Principal name is the username set during JWT auth
        // We look up the user by username to get their ID
        Users user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found."));
        return user.getId();
    }

}
