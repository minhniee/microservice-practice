package org.example.productservice.service;

import org.example.productservice.entity.Product;
import org.example.productservice.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<Product> findAll() {
        return repository.findAll();
    }

    public Optional<Product> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public Product create(Product product) {
        return repository.save(product);
    }

    @Transactional
    public Optional<Product> update(Long id, Product updates) {
        return repository.findById(id)
                .map(existing -> {
                    if (updates.getName() != null) existing.setName(updates.getName());
                    if (updates.getPrice() != null) existing.setPrice(updates.getPrice());
                    if (updates.getQuantity() != null) existing.setQuantity(updates.getQuantity());
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
