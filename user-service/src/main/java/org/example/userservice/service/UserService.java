package org.example.userservice.service;

import org.example.userservice.entity.User;
import org.example.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public List<User> findAll() {
        return repository.findAll();
    }

    public Optional<User> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public User create(User user) {
        return repository.save(user);
    }

    @Transactional
    public Optional<User> update(Long id, User updates) {
        return repository.findById(id)
                .map(existing -> {
                    if (updates.getName() != null) existing.setName(updates.getName());
                    if (updates.getEmail() != null) existing.setEmail(updates.getEmail());
                    return repository.save(existing);
                });
    }

    @Transactional
    public boolean delete(Long id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }
}
