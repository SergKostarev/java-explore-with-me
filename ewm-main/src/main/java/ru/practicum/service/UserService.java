package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dao.UserRepository;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.exception.DataIntegrityException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserDto post(NewUserRequest newUserRequest) {
        String email = newUserRequest.getEmail();
        boolean emailExists = userRepository.findByEmailIgnoreCase(email).isPresent();
        if (emailExists) {
            throw new DataIntegrityException(String.format("Email %s already exists.", email), email);
        }
        return UserMapper.toUserDto(
                saveUser(UserMapper.toUser(newUserRequest)));
    }

    @Transactional
    public void delete(long id) {
        User user = getById(id);
        userRepository.delete(user);
    }

    public List<UserDto> get(List<Long> ids, int from, int size) {
        List<User> users;
        if (ids == null || ids.isEmpty()) {
            users = userRepository.get(from, size);
        } else {
            users = userRepository.get(ids);
        }
        return users.stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    public User getById(long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new NotFoundException(String.format("User with id=%d was not found.", id), id);
        }
        return user.get();
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
