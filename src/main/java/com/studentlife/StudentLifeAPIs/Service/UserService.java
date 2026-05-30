package com.studentlife.StudentLifeAPIs.Service;


import com.studentlife.StudentLifeAPIs.DTO.Request.UserCreateRequest;
import com.studentlife.StudentLifeAPIs.DTO.Request.UserUpdateRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.PaginatedResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.UserResponse;
import org.springframework.data.domain.Sort;

public interface UserService {

    PaginatedResponse<UserResponse> getAllUsers(int page, int size, String search, String role, Sort sort);

    UserResponse getUserById(Long id);

    UserResponse createUser(UserCreateRequest request);

    UserResponse updateUser(Long id, UserUpdateRequest request);

    ApiResponse<UserResponse> getProfileInfo();

    ApiResponse<UserResponse> updateUserProfile(UserUpdateRequest request);

    void disableUser(Long id);

    void deleteUser(Long id);
}