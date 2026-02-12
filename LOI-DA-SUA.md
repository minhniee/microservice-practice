# Danh sách lỗi đã gặp và cách đã sửa

Tài liệu này liệt kê **tất cả lỗi** từ lúc cấu hình lại dự án đến hiện tại, kèm nguyên nhân và cách xử lý.

---

## 1. Build / POM – Dự án không build được

### 1.1. Discovery-server: dependency thiếu version

- **Lỗi:**  
  `'dependencies.dependency.version' for org.springframework.boot:spring-boot-starter-webmvc:jar is missing`

- **Nguyên nhân:**  
  Artifact `spring-boot-starter-webmvc` **không tồn tại** trong Spring Boot (tên đúng là `spring-boot-starter-web`), nên parent không quản lý version → Maven báo thiếu version.

- **Đã sửa:**  
  Đổi `spring-boot-starter-webmvc` → `spring-boot-starter-web` và không ghi version (để parent quản lý).

- **File:** `discovery-server/pom.xml`

---

### 1.2. Api-gateway: Spring Boot version sai

- **Lỗi:**  
  Build/run dùng Spring Boot **4.0.2** → dễ lỗi tương thích với Spring Cloud; Config Server khi chạy báo lỗi `LifecycleMvcEndpointAutoConfiguration` / `WebMvcAutoConfiguration` (ClassNotFoundException).

- **Nguyên nhân:**  
  Api-gateway khai báo parent version `4.0.2`; Spring Boot 4.x đổi package/class → không khớp với Spring Cloud Config và một số auto-config.

- **Đã sửa:**  
  Đổi parent Spring Boot từ `4.0.2` → `3.4.2` (thống nhất với các module khác).

- **File:** `api-gateway/pom.xml`

---

### 1.3. Config-server & User/Product service: dependency sai tên

- **Lỗi:**  
  Dùng `spring-boot-starter-webmvc` (artifact không chuẩn) và có thể gây thiếu version hoặc không đúng với parent.

- **Đã sửa:**  
  Đổi `spring-boot-starter-webmvc` → `spring-boot-starter-web`, bỏ version explicit.

- **File:** `config-server/pom.xml`, `user-service/pom.xml`, `product-service/pom.xml`

---

### 1.4. Không có root POM cho toàn dự án

- **Lỗi:**  
  Không build được tất cả module từ thư mục gốc bằng một lệnh Maven.

- **Đã sửa:**  
  Tạo `pom.xml` ở thư mục gốc, khai báo các module: config-server, discovery-server, api-gateway, user-service, product-service.

- **File:** `pom.xml` (root)

---

## 2. Cấu hình ứng dụng – Thiếu port & Eureka

### 2.1. User-service & Product-service: thiếu port và Eureka client

- **Lỗi:**  
  Service không có `server.port` và `eureka.client` trong file cấu hình local → khi không dùng Config Server hoặc config chưa load, không có port và không đăng ký Eureka.

- **Đã sửa:**  
  Thêm vào `application.yml` (và config-repo):
  - `server.port` (8089 cho user-service, 8082 cho product-service)
  - `eureka.client.service-url.defaultZone: http://localhost:8761/eureka`

- **File:**  
  `user-service/src/main/resources/application.yml`,  
  `product-service/src/main/resources/application.yml`,  
  `config-server/config-repo/user-service.yml`,  
  `config-server/config-repo/product-service.yml`

---

## 3. API Gateway – Không gọi được User/Product service

### 3.1. Gateway không có route khi không dùng Config Server

- **Lỗi:**  
  Gọi `http://localhost:8080/users/1` hoặc `/products/1` không đi đúng service (404 hoặc không route).

- **Nguyên nhân:**  
  Route (`/users/**`, `/products/**`) chỉ khai báo trong **config-repo/api-gateway.yml**. File **application.yml local** của api-gateway không có `spring.cloud.gateway.routes` → khi Config Server không chạy hoặc không load được config, Gateway không có route.

- **Đã sửa:**  
  Thêm đầy đủ `spring.cloud.gateway.discovery.locator` và `spring.cloud.gateway.routes` (user-service, product-service) vào **api-gateway/src/main/resources/application.yml**.

- **File:** `api-gateway/src/main/resources/application.yml`

---

### 3.2. Tên service trong route (lb://) không khớp Eureka

- **Lỗi:**  
  Route dùng `lb://USER-SERVICE` và `lb://PRODUCT-SERVICE` (chữ in hoa). Một số phiên bản LoadBalancer so khớp tên service **phân biệt hoa thường** → không tìm thấy instance (application name đăng ký là `user-service`, `product-service`).

- **Đã sửa:**  
  Đổi thành `lb://user-service` và `lb://product-service` trong cả file local và config-repo.

- **File:**  
  `api-gateway/src/main/resources/application.yml`,  
  `config-server/config-repo/api-gateway.yml`

---

### 3.3. Gateway thiếu LoadBalancer (lb:// không hoạt động)

- **Lỗi:**  
  Gateway vẫn không forward được request tới user-service/product-service dù Eureka đã chạy (có thể 503 hoặc không resolve được `lb://...`).

- **Nguyên nhân:**  
  Để dùng scheme `lb://` với Eureka, **bắt buộc** có `spring-cloud-starter-loadbalancer` trên classpath. Không có dependency này thì Gateway không resolve được tên service thành host:port.

- **Đã sửa:**  
  Thêm dependency `spring-cloud-starter-loadbalancer` vào api-gateway.

- **File:** `api-gateway/pom.xml`

---

### 3.4. Eureka đăng ký hostname, Gateway gọi không được (Windows)

- **Lỗi:**  
  Trên một số môi trường (đặc biệt Windows), instance đăng ký với **hostname**; Gateway khi gọi theo hostname có thể không resolve được.

- **Đã sửa:**  
  Thêm `eureka.instance.prefer-ip-address: true` cho user-service và product-service (cả application.yml local và config-repo) để đăng ký bằng IP (127.0.0.1).

- **File:**  
  `user-service/src/main/resources/application.yml`,  
  `product-service/src/main/resources/application.yml`,  
  `config-server/config-repo/user-service.yml`,  
  `config-server/config-repo/product-service.yml`

---

## 4. Thứ tự khởi động – Connection refused tới Eureka

### 4.1. Connection refused khi kết nối localhost:8761

- **Lỗi:**  
  `Connect to http://localhost:8761 [...] failed: Connection refused`  
  (heartbeat, fetch registry, unregister đều lỗi khi Gateway/User/Product cố nói chuyện với Eureka.)

- **Nguyên nhân:**  
  **Eureka Server (discovery-server)** chưa chạy tại thời điểm api-gateway (và các Eureka client khác) khởi động. Port 8761 không có process nào lắng nghe → connection refused.

- **Đã sửa:**  
  - Giải thích thứ tự chạy bắt buộc: **Discovery Server (Eureka) phải chạy trước**, sau đó mới chạy api-gateway, user-service, product-service.  
  - Tạo script `start-services.ps1` để tự động mở từng terminal và chạy đúng thứ tự (Config → Eureka → User → Product → Gateway).  
  - Tạo `RUN.md` hướng dẫn chạy tay và chạy bằng script.

- **File:**  
  `start-services.ps1`, `RUN.md`

---

## Tóm tắt theo nhóm

| Nhóm            | Lỗi chính                                      | Cách đã sửa                                      |
|-----------------|-------------------------------------------------|--------------------------------------------------|
| **POM / Build** | webmvc sai tên, thiếu version, Boot 4.0.2        | Dùng `spring-boot-starter-web`, Boot 3.4.2, root pom |
| **Config**      | Thiếu port, Eureka client                       | Thêm server.port + eureka.client (và config-repo) |
| **Gateway**     | Không có route local, lb:// sai tên            | Route trong application.yml, lb://user-service   |
| **Gateway**     | lb:// không resolve                             | Thêm spring-cloud-starter-loadbalancer           |
| **Eureka**      | Hostname không resolve (Windows)                | prefer-ip-address: true                          |
| **Khởi động**   | Connection refused 8761                         | Chạy Eureka trước, script + RUN.md               |

Nếu sau này gặp lỗi mới, có thể bổ sung vào file này và ghi rõ thời điểm/phiên bản để dễ đối chiếu.
