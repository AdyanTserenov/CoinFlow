package com.example.coinflow.services;

import com.example.coinflow.models.Category;
import com.example.coinflow.models.User;
import com.example.coinflow.repositories.CategoryRepository;
import com.example.coinflow.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public List<Category> getAllCategories(User user) {
        return categoryRepository.findByUserOrIsDefault(user, true);
    }

    public Category createCategory(Category category, User user) {
        User realUser = userRepository.findUserByUsername(user.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        category.setUser(realUser);
        category.setDefault(false);
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, Category updatedCategory, User user) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (existingCategory.getUser() == null || !existingCategory.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized to update this category");
        }

        existingCategory.setName(updatedCategory.getName());
        existingCategory.setDescription(updatedCategory.getDescription());
        return categoryRepository.save(existingCategory);
    }

    public void deleteCategory(Long id, User user) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (category.getUser() == null || !category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized to delete this category");
        }

        categoryRepository.delete(category);
    }

    @Transactional
    public void initializeDefaultCategories() {
        if (categoryRepository.findByIsDefaultTrue().isEmpty()) {
            Category food = new Category();
            food.setName("Еда");
            food.setDescription("Расходы на продукты питания и рестораны");
            food.setDefault(true);
            categoryRepository.save(food);

            Category transport = new Category();
            transport.setName("Транспорт");
            transport.setDescription("Расходы на транспорт, такси, общественный транспорт");
            transport.setDefault(true);
            categoryRepository.save(transport);

            Category housing = new Category();
            housing.setName("Жилье");
            housing.setDescription("Расходы на аренду, коммунальные услуги, ремонт");
            housing.setDefault(true);
            categoryRepository.save(housing);

            Category entertainment = new Category();
            entertainment.setName("Развлечения");
            entertainment.setDescription("Расходы на развлечения, хобби, досуг");
            entertainment.setDefault(true);
            categoryRepository.save(entertainment);

            Category shopping = new Category();
            shopping.setName("Шоппинг");
            shopping.setDescription("Расходы на одежду, обувь, аксессуары");
            shopping.setDefault(true);
            categoryRepository.save(shopping);
        }
    }
} 