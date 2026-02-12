# Best practices đã áp dụng (Spring Cloud)

## 1. Cấu hình tập trung (Config Server)

- **Config-repo** là nguồn chân lý cho port, Eureka, routes. Các service chỉ cần `spring.application.name` + `spring.config.import`.
- **Fallback** trong từng `application.yml` (port, eureka, routes) dùng khi Config Server không chạy → vẫn chạy được local/dev.

## 2. Port động (server.port: 0)

- Trong **config-repo**, `user-service` và `product-service` dùng **`server.port: 0`**.
- Spring Boot gán **port ngẫu nhiên** (ví dụ 54321). Eureka nhận đúng host:port khi instance đăng ký → Gateway và client gọi qua **tên service** (`lb://user-service`), không phụ thuộc port cố định.
- **Lợi ích:** Chạy nhiều instance cùng lúc (scale) không đụng port; không cần khai báo port cố định cho từng instance.

**Lưu ý:** Khi Config Server **không** chạy, fallback trong code vẫn dùng port cố định (8089, 8082) để dễ debug.

## 3. Eureka instance

- **`prefer-ip-address: true`** → Instance đăng ký bằng IP thay vì hostname (tránh lỗi resolve trên Windows/Docker).
- **`instance-id: ${spring.application.name}:${random.value}`** → Mỗi instance có ID duy nhất; Eureka phân biệt nhiều instance cùng service (vd: 2 user-service).

## 4. Health check (Actuator)

- **user-service** và **product-service** có **spring-boot-starter-actuator**.
- Trong config-repo bật **`management.endpoints.web.exposure.include: health`** → Eureka gọi `/actuator/health` để đánh dấu instance UP/DOWN; instance lỗi sẽ bị loại khỏi load balance.

## 5. API Gateway

- **Port cố định (8080)** cho Gateway → Client luôn gọi một địa chỉ cố định.
- **Routes** theo tên service: `lb://user-service`, `lb://product-service`, `lb://order-service` (sẵn sàng khi có order-service).
- **Discovery locator** bật + `lower-case-service-id: true` → Có thể gọi qua `/user-service/...`, `/product-service/...` ngoài route tùy chỉnh.

## 6. Thứ tự khởi động

1. **Config Server** (8888) – nguồn cấu hình.
2. **Eureka** (8761) – service discovery.
3. **User / Product / Order** – lấy config từ Config Server, đăng ký Eureka (port 0 khi dùng config-repo).
4. **API Gateway** (8080) – lấy config + routes, gọi backend qua Eureka.

## 7. Tóm tắt

| Thành phần        | Best practice |
|-------------------|----------------|
| Config            | Tập trung ở config-repo, fallback tối thiểu trong từng service. |
| Port microservice | `server.port: 0` trong config-repo; Eureka + Gateway gọi theo tên. |
| Port Gateway      | Cố định 8080 để client biết điểm vào. |
| Eureka            | `prefer-ip-address`, `instance-id` duy nhất. |
| Health            | Actuator health cho Eureka health check. |
| Routes            | Gateway route theo `lb://service-name`, thêm order-service sẵn. |
