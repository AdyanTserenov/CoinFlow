package com.example.coinflow.repositories;

import com.example.coinflow.models.Category;
import com.example.coinflow.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUser(User user);
    List<Category> findByIsDefaultTrue();
    List<Category> findByUserOrIsDefault(User user, boolean isDefault);
} 