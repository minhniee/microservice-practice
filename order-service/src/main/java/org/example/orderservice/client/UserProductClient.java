package org.example.orderservice.client;

import org.example.orderservice.dto.ProductSummary;
import org.example.orderservice.dto.UserSummary;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserProductClient {

    private final RestTemplate restTemplateUser;
    private final RestTemplate restTemplateProduct;

    public UserProductClient(
            @Qualifier("restTemplateUser") RestTemplate restTemplateUser,
            @Qualifier("restTemplateProduct") RestTemplate restTemplateProduct) {
        this.restTemplateUser = restTemplateUser;
        this.restTemplateProduct = restTemplateProduct;
    }

    /** Call user-service over mTLS. */
    public UserSummary getUser(Long userId) {
        String url = "https://user-service/users/" + userId;
        return restTemplateUser.getForObject(url, UserSummary.class);
    }

    /** Call product-service over one-way TLS. */
    public ProductSummary getProduct(Long productId) {
        String url = "https://product-service/products/" + productId;
        return restTemplateProduct.getForObject(url, ProductSummary.class);
    }
}
