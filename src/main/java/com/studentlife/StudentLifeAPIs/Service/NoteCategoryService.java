package com.studentlife.StudentLifeAPIs.Service;

import com.studentlife.StudentLifeAPIs.DTO.Request.NoteCategoryRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.NoteCategoryResponse;

import java.util.List;

public interface NoteCategoryService {

    ApiResponse<NoteCategoryResponse> createCategory(NoteCategoryRequest request);

    ApiResponse<List<NoteCategoryResponse>> getMyCategories();

    ApiResponse<NoteCategoryResponse> updateCategory(Long id, NoteCategoryRequest request);

    ApiResponse<?> deleteCategory(Long id);
}
