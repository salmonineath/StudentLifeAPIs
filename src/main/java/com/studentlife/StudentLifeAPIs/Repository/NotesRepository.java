package com.studentlife.StudentLifeAPIs.Repository;

import com.studentlife.StudentLifeAPIs.Entity.Notes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotesRepository extends JpaRepository<Notes, Long> {

    Optional<Notes> findByIdAndUserId(Long id, Long userId);

    List<Notes> findAllByCategoryId(Long categoryId);

    @Query("""
            SELECT n FROM Notes n
            WHERE n.user.id = :userId
            AND (:categoryId IS NULL OR n.category.id = :categoryId)
            AND (:search IS NULL OR LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%'))
                                 OR LOWER(n.content) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY n.updatedAt DESC
            """)
    Page<Notes> findByFilter(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("search") String search,
            Pageable pageable
    );
}
