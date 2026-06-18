package com.studentlife.StudentLifeAPIs.Controller;

import com.studentlife.StudentLifeAPIs.DTO.Request.NoteCategoryRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.Service.NoteCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notes/categories")
@RequiredArgsConstructor
public class NoteCategoryController {

    private final NoteCategoryService noteCategoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createCategory(@RequestBody @Valid NoteCategoryRequest request) {
        return ResponseEntity.status(201).body(noteCategoryService.createCategory(request));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getMyCategories() {
        return ResponseEntity.ok(noteCategoryService.getMyCategories());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateCategory(
            @PathVariable Long id,
            @RequestBody @Valid NoteCategoryRequest request
    ) {
        return ResponseEntity.ok(noteCategoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteCategory(@PathVariable Long id) {
        return ResponseEntity.ok(noteCategoryService.deleteCategory(id));
    }
}
