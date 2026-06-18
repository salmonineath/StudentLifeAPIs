package com.studentlife.StudentLifeAPIs.Repository;

import com.studentlife.StudentLifeAPIs.Entity.NoteCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoteCategoryRepository extends JpaRepository<NoteCategory, Long> {

    List<NoteCategory> findAllByUserId(Long userId);

    Optional<NoteCategory> findByIdAndUserId(Long id, Long userId);
}
