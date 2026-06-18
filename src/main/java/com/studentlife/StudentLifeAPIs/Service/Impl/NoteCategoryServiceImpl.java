package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.DTO.Request.NoteCategoryRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.NoteCategoryResponse;
import com.studentlife.StudentLifeAPIs.Entity.NoteCategory;
import com.studentlife.StudentLifeAPIs.Mapper.NoteCategoryMapper;
import com.studentlife.StudentLifeAPIs.Repository.NoteCategoryRepository;
import com.studentlife.StudentLifeAPIs.Repository.NotesRepository;
import com.studentlife.StudentLifeAPIs.Service.NoteCategoryService;
import com.studentlife.StudentLifeAPIs.Utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.notFound;

@Service
@RequiredArgsConstructor
public class NoteCategoryServiceImpl implements NoteCategoryService {

    private final NoteCategoryRepository noteCategoryRepository;
    private final NotesRepository notesRepository;
    private final NoteCategoryMapper noteCategoryMapper;
    private final AuthUtil authUtil;

    @Override
    public ApiResponse<NoteCategoryResponse> createCategory(NoteCategoryRequest request) {
        var currentUser = authUtil.getAuthenticatedUser();

        NoteCategory category = noteCategoryMapper.toEntity(request);
        category.setUser(currentUser);
        noteCategoryRepository.save(category);

        return new ApiResponse<>(
            201,
            true,
            "Category created successfully.", 
            noteCategoryMapper.toResponse(category)
        );
    }

    @Override
    public ApiResponse<List<NoteCategoryResponse>> getMyCategories() {
        var currentUser = authUtil.getAuthenticatedUser();

        List<NoteCategoryResponse> categories = noteCategoryRepository
                .findAllByUserId(currentUser.getId())
                .stream()
                .map(noteCategoryMapper::toResponse)
                .toList();

        return new ApiResponse<>(
            200, 
            true, 
            "Categories retrieved successfully.", 
            categories
        );
    }

    @Override
    public ApiResponse<NoteCategoryResponse> updateCategory(Long id, NoteCategoryRequest request) {
        var currentUser = authUtil.getAuthenticatedUser();

        NoteCategory category = noteCategoryRepository
                .findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> notFound("Category not found."));

        category.setName(request.getName());
        category.setColor(request.getColor());
        noteCategoryRepository.save(category);

        return new ApiResponse<>(    
            200, 
            true, 
            "Category updated successfully.", 
            noteCategoryMapper.toResponse(category)
        );
    }

    @Override
    @Transactional
    public ApiResponse<?> deleteCategory(Long id) {
        var currentUser = authUtil.getAuthenticatedUser();

        NoteCategory category = noteCategoryRepository
                .findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> notFound("Category not found."));

        notesRepository.findAllByCategoryId(id)
                .forEach(note -> note.setCategory(null));

        noteCategoryRepository.delete(category);

        return new ApiResponse<>(
            200, 
            true, 
            "Category deleted successfully."
        );
    }
}
