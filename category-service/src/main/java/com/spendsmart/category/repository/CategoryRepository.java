package com.spendsmart.category.repository;

import com.spendsmart.category.entity.Category;
import com.spendsmart.category.entity.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserIdOrderByNameAsc(Long userId);

    List<Category> findByUserIdAndTypeOrderByNameAsc(Long userId, CategoryType type);

    Optional<Category> findByCategoryIdAndUserId(Long categoryId, Long userId);

    boolean existsByUserIdAndNameIgnoreCaseAndType(Long userId, String name, CategoryType type);

    List<Category> findByIsDefaultTrue();

    long countByUserId(Long userId);

    void deleteByCategoryIdAndUserId(Long categoryId, Long userId);
}
