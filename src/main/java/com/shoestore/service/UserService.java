package com.shoestore.service;

import com.shoestore.entity.User;
import com.shoestore.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Регистрация пользователя
    public User register(String email, String password, String name) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        User user = new User(email, password, name);
        return userRepository.save(user);
    }

    // Авторизация
    public Optional<User> login(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent() && user.get().getPassword().equals(password)) {
            return user;
        }
        return Optional.empty();
    }

    // Получить всех пользователей
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Найти по ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Обновить пользователя
    public User updateUser(Long id, String name, String phone) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        user.setName(name);
        user.setPhone(phone);
        return userRepository.save(user);
    }
}