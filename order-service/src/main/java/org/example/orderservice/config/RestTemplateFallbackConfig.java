package org.example.orderservice.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Fallback RestTemplate beans when ssl.enabled is not true (e.g. local dev without certs).
 * Use http:// for backend calls in that case.
 */
@Configuration
public class RestTemplateFallbackConfig {

    @Bean("restTemplateUser")
    @LoadBalanced
    @ConditionalOnMissingBean(name = "restTemplateUser")
    public RestTemplate restTemplateUser() {
        return new RestTemplate();
    }

    @Bean("restTemplateProduct")
    @LoadBalanced
    @ConditionalOnMissingBean(name = "restTemplateProduct")
    public RestTemplate restTemplateProduct() {
        return new RestTemplate();
    }
}
