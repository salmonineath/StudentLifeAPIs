package com.studentlife.StudentLifeAPIs.Repository;

import com.studentlife.StudentLifeAPIs.Entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    boolean existsByEmail(String email);

    Optional<Users> findByEmail(String email);

    Optional<Users> findByUsername(String username);

    @Query(
        value = "SELECT DISTINCT u FROM Users u LEFT JOIN u.roles r WHERE " +
                "(:search IS NULL OR LOWER(u.fullname) LIKE LOWER(CONCAT('%', :search, '%')) " +
                " OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) " +
                " OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                "AND (:role IS NULL OR r.name = :role)",
        countQuery = "SELECT COUNT(DISTINCT u.id) FROM Users u LEFT JOIN u.roles r WHERE " +
                     "(:search IS NULL OR LOWER(u.fullname) LIKE LOWER(CONCAT('%', :search, '%')) " +
                     " OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) " +
                     " OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                     "AND (:role IS NULL OR r.name = :role)"
    )
    Page<Users> search(@Param("search") String search, @Param("role") String role, Pageable pageable);
}
