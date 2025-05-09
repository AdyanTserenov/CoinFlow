package com.example.coinflow.configurators;

import com.example.coinflow.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DefaultCategoriesInitializer implements CommandLineRunner {
    private final CategoryService categoryService;

    @Autowired
    public DefaultCategoriesInitializer(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public void run(String... args) {
        categoryService.initializeDefaultCategories();
    }
}