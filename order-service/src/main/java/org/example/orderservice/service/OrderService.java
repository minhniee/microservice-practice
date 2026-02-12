package org.example.orderservice.service;

import org.example.orderservice.entity.Order;
import org.example.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public List<Order> findAll() {
        return repository.findAll();
    }

    public Optional<Order> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public Order create(Order order) {
        return repository.save(order);
    }

    @Transactional
    public Optional<Order> updateStatus(Long id, String status) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setStatus(status);
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
