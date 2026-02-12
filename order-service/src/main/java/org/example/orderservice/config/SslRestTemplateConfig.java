package org.example.orderservice.config;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;

@Configuration
public class SslRestTemplateConfig {

    @Value("${ssl.certs.path:certs}")
    private String certsPath;

    @Value("${ssl.keystore.password:changeit}")
    private String keystorePassword;

    @Value("${ssl.truststore.password:changeit}")
    private String truststorePassword;

    private KeyStore loadKeystore(String filePath, String password) throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(filePath)) {
            ks.load(fis, password.toCharArray());
        }
        return ks;
    }

    private String resolvePath(String relativePath) {
        Path base = Paths.get(certsPath.replace("file:", ""));
        return base.resolve(relativePath).toAbsolutePath().toString();
    }

    /** One-way TLS: truststore only (for calling product-service). */
    @Bean("restTemplateProduct")
    @LoadBalanced
    @ConditionalOnProperty(name = "ssl.enabled", havingValue = "true")
    public RestTemplate restTemplateProduct() throws Exception {
        String trustPath = resolvePath("truststore.p12");
        KeyStore trustStore = loadKeystore(trustPath, truststorePassword);
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(trustStore, null)
                .build();
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext);
        HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslSocketFactory)
                .build();
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
    }

    /** mTLS: truststore + client keystore (for calling user-service). */
    @Bean("restTemplateUser")
    @LoadBalanced
    @ConditionalOnProperty(name = "ssl.enabled", havingValue = "true")
    public RestTemplate restTemplateUser() throws Exception {
        String trustPath = resolvePath("truststore.p12");
        String keyPath = resolvePath("client.p12");
        KeyStore trustStore = loadKeystore(trustPath, truststorePassword);
        KeyStore keyStore = loadKeystore(keyPath, keystorePassword);
        SSLContext sslContext = SSLContextBuilder.create()
                .loadKeyMaterial(keyStore, keystorePassword.toCharArray())
                .loadTrustMaterial(trustStore, null)
                .build();
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext);
        HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslSocketFactory)
                .build();
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
    }
}
