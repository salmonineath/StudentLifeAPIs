package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.Entity.UserProfile;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Repository.UploadProfileRepository;
import com.studentlife.StudentLifeAPIs.Repository.UserRepository;
import com.studentlife.StudentLifeAPIs.Service.CloudinaryService;
import org.springframework.stereotype.Service;

import com.studentlife.StudentLifeAPIs.Service.UploadProfileService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.*;

@Service
@RequiredArgsConstructor
public class UploadProfileServiceImpl implements UploadProfileService{

    private final CloudinaryService cloudinaryService;
    private final UploadProfileRepository uploadProfileRepository;
    private final UserRepository userRepository;

    @Override
    public List<String> uploadUserProfile(Long userId, MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw badRequest("No file provided");
        }

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> notFound("User not found"));

        try {

            String contentType =  file.getContentType();
            if (contentType ==  null ||
                    (!contentType.equals("image/jpeg") &&
                            !contentType.equals("image/png") &&
                            !contentType.equals("image/jpg"))) {
                throw badRequest("Only png, jpg and jpeg are supported");
            }

            Map<?, ?> uploadResult = cloudinaryService.upload(file, "folder");

            String imageUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            if (user.getProfile() != null && user.getProfile().getPublicId() != null) {
                cloudinaryService.delete(user.getProfile().getPublicId());
            }

            UserProfile userProfile = new UserProfile();
            userProfile.setUrls(imageUrl);
            userProfile.setPublicId(publicId);
            userProfile.setUser(user);

            
        } catch (IOException e) {
            throw internal("Failed to upload profile!");
        }

        return List.of();
    }

    @Override
    public void deleteProfile(Long profileId, Long userId) {

    }
}
