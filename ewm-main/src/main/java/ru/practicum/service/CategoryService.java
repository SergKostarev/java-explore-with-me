package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dao.CategoryRepository;
import ru.practicum.dao.EventRepository;
import ru.practicum.dto.CategoryDto;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.DataIntegrityException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    private final EventRepository eventRepository;

    public List<CategoryDto> get(int from, int size) {
        return categoryRepository
                .get(from, size)
                .stream()
                .map(CategoryMapper::toCategoryDto)
                .toList();
    }

    public Category getById(long id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isEmpty()) {
            throw new NotFoundException(String.format("Category with id=%d was not found", id), id);
        }
        return category.get();
    }

    public CategoryDto getDtoById(long id) {
        return CategoryMapper.toCategoryDto(getById(id));
    }

    @Transactional()
    public CategoryDto post(CategoryDto categoryDto) {
        String name = categoryDto.getName();
        boolean categoryExists = categoryRepository.findByNameIgnoreCase(name).isPresent();
        if (categoryExists) {
            throw new DataIntegrityException(String.format("Category %s already exists.", name), name);
        }
        Category savedCategory = categoryRepository.save(CategoryMapper.toCategory(categoryDto));
        return CategoryMapper.toCategoryDto(savedCategory);
    }

    @Transactional()
    public CategoryDto update(long catId, CategoryDto categoryDto) {
        Category category = getById(catId);
        String name = categoryDto.getName();
        Optional<Category> existCategory = categoryRepository.findByNameIgnoreCase(name);
        boolean categoryExists = existCategory.isPresent()
                && !existCategory.get().getName().equals(category.getName());
        if (categoryExists) {
            throw new DataIntegrityException(String.format("Category %s already exists.", name), name);
        }
        category.setName(categoryDto.getName());
        Category savedCategory = categoryRepository.save(category);
        return CategoryMapper.toCategoryDto(savedCategory);
    }

    @Transactional()
    public void delete(long catId) {
        Category category = getById(catId);
        if (!eventRepository.findByCategoryId(catId).isEmpty()) {
            throw new ConditionsNotMetException(String.format("Events with category with id=%d exist.", catId),
                    String.valueOf(catId));
        }
        categoryRepository.delete(category);
    }
}
