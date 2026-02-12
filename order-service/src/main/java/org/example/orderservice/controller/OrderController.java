package org.example.orderservice.controller;

import org.example.orderservice.dto.OrderDetailResponse;
import org.example.orderservice.dto.ProductSummary;
import org.example.orderservice.dto.UserSummary;
import org.example.orderservice.entity.Order;
import org.example.orderservice.client.UserProductClient;
import org.example.orderservice.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserProductClient userProductClient;

    public OrderController(OrderService orderService, UserProductClient userProductClient) {
        this.orderService = orderService;
        this.userProductClient = userProductClient;
    }

    @GetMapping
    public List<Order> list() {
        return orderService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getById(@PathVariable Long id) {
        return orderService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<OrderDetailResponse> getDetail(@PathVariable Long id) {
        return orderService.findById(id)
                .map(order -> {
                    OrderDetailResponse resp = new OrderDetailResponse();
                    resp.setOrder(order);
                    try {
                        UserSummary user = userProductClient.getUser(order.getUserId());
                        resp.setUser(user);
                    } catch (Exception e) {
                        resp.setUser(null);
                    }
                    try {
                        ProductSummary product = userProductClient.getProduct(order.getProductId());
                        resp.setProduct(product);
                    } catch (Exception e) {
                        resp.setProduct(null);
                    }
                    return ResponseEntity.ok(resp);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody Order order) {
        Order created = orderService.create(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return orderService.updateStatus(id, status)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return orderService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
