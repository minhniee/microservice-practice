package org.example.orderservice.dto;

import org.example.orderservice.entity.Order;

public class OrderDetailResponse {
    private Order order;
    private UserSummary user;
    private ProductSummary product;

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public UserSummary getUser() { return user; }
    public void setUser(UserSummary user) { this.user = user; }
    public ProductSummary getProduct() { return product; }
    public void setProduct(ProductSummary product) { this.product = product; }
}
