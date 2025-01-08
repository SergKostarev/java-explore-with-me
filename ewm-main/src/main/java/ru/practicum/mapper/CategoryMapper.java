package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.CategoryDto;
import ru.practicum.model.Category;

@UtilityClass
public class CategoryMapper {

    public static CategoryDto toCategoryDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }

    public static Category toCategory(CategoryDto categoryDto) {
        Category category = new Category();
        category.setName(categoryDto.getName());
        return category;
    }

}
