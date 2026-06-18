package com.studentlife.StudentLifeAPIs.Utils;

import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Jwt.JwtService;
import com.studentlife.StudentLifeAPIs.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.security.Principal;

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

    public Long getUserIdFromPrincipal(Principal principal) {
        if (principal == null) {
            throw unauthorized("Not authenticated.");
        }
        if (principal instanceof UsernamePasswordAuthenticationToken auth &&
                auth.getPrincipal() instanceof Users user) {
            return user.getId();
        }
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> unauthorized("User not found."))
                .getId();
    }

}
