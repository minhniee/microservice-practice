# TLS certificates for microservices (dev only)

Run `generate-certs.ps1` (PowerShell) or `generate-certs.sh` (Bash) from this directory to generate:

- **ca.p12** – CA keystore (used to sign server and client certs)
- **truststore.p12** – Contains CA cert; used by api-gateway and order-service to verify backend servers
- **user-server.p12** – Server cert for user-service (mTLS: requires client cert)
- **product-server.p12** – Server cert for product-service (one-way TLS)
- **order-server.p12** – Server cert for order-service (one-way TLS)
- **client.p12** – Client cert for order-service and api-gateway when calling user-service (mTLS)

Password for all keystores: `changeit` (override with env or config in production).

User-service must be configured with `server.ssl.trust-store` pointing to truststore.p12 (or ca.p12) so it can verify client certificates.
