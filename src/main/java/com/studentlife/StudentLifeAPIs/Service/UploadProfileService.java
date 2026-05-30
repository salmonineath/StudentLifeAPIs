package com.studentlife.StudentLifeAPIs.Service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface UploadProfileService {
    
    List<String> uploadUserProfile(Long userId, MultipartFile file);

    void deleteProfile(Long profileId, Long userId);
}