package org.example.apigateway.config;

import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.config.HttpClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;

@Configuration
public class GatewaySslConfig {

    @Value("${ssl.certs.path:certs}")
    private String certsPath;

    @Value("${ssl.keystore.password:changeit}")
    private String keystorePassword;

    @Value("${ssl.truststore.password:changeit}")
    private String truststorePassword;

    @Bean
    @ConditionalOnProperty(name = "ssl.enabled", havingValue = "true", matchIfMissing = false)
    public HttpClientCustomizer sslHttpClientCustomizer() throws Exception {
        Path certBase = resolveCertsDir();
        String trustPath = certBase.resolve("truststore.p12").toAbsolutePath().toString();
        String clientPath = certBase.resolve("client.p12").toAbsolutePath().toString();

        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(trustPath)) {
            trustStore.load(fis, truststorePassword.toCharArray());
        }
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(clientPath)) {
            keyStore.load(fis, keystorePassword.toCharArray());
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keystorePassword.toCharArray());

        var sslContext = SslContextBuilder.forClient()
                .keyManager(kmf)
                .trustManager(tmf)
                .build();

        return httpClient -> httpClient.secure(spec -> spec.sslContext(sslContext));
    }

    /**
     * Resolve the certs directory robustly so the app can be started
     * from either repo root or a module subdirectory.
     */
    private Path resolveCertsDir() {
        String base = certsPath.replace("file:", "");
        Path start = Paths.get(base);

        // 1) direct path (as provided)
        if (hasRequiredFiles(start)) return start;

        // 2) common relative fallback when running from a module directory
        Path parentCerts = Paths.get("..", base);
        if (hasRequiredFiles(parentCerts)) return parentCerts;

        // 3) walk up a few levels looking for "<something>/certs/truststore.p12"
        Path cwd = Paths.get(".").toAbsolutePath().normalize();
        Path p = cwd;
        for (int i = 0; i < 5 && p != null; i++) {
            Path candidate = p.resolve(base);
            if (hasRequiredFiles(candidate)) return candidate;
            p = p.getParent();
        }

        throw new IllegalStateException("Cannot find truststore/client keystore. " +
                "Tried ssl.certs.path='" + certsPath + "' and common fallbacks. " +
                "Set CERTS_PATH to the absolute certs directory, e.g. " +
                "'CERTS_PATH=C:\\\\Users\\\\ADMIN\\\\IdeaProjects\\\\microservice\\\\certs'.");
    }

    private boolean hasRequiredFiles(Path dir) {
        if (dir == null) return false;
        File trust = dir.resolve("truststore.p12").toFile();
        File client = dir.resolve("client.p12").toFile();
        return trust.isFile() && client.isFile();
    }
}
