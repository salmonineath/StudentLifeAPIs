package com.studentlife.StudentLifeAPIs.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {
    private List<T> items;
    private PaginationMeta pagination;

    /**
     * Static factory — converts any Spring Page<T> into a clean PaginatedResponse<T>.
     * Map the page to DTOs first (e.g. {@code page.map(mapper::toResponse)}), then pass it here.
     */
    public static <T> PaginatedResponse<T> from(Page<T> page) {
        PaginationMeta meta = new PaginationMeta(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
        return new PaginatedResponse<>(page.getContent(), meta);
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationMeta {
        private int currentPage;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;
    }
}