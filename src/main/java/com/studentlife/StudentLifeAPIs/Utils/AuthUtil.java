package com.studentlife.StudentLifeAPIs.Utils;

import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.unauthorized;

@Component
@RequiredArgsConstructor
public class AuthUtil {

    private final JwtService jwtService;

    public Users getAuthenticatedUser() {
        Users currentUser = jwtService.getCurrentUser();

        if (currentUser == null) {
            throw unauthorized("Authentication required - no authenticated user found");
        }

        return currentUser;
    }
}
