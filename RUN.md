# Cách chạy dự án Microservice

## Lỗi "Connection refused" đến localhost:8761

**Nguyên nhân:** API Gateway (và User/Product service) cần **Eureka Server** đang chạy tại `http://localhost:8761`.  
Nếu bạn chạy Gateway trước khi chạy Discovery Server → **Connection refused**.

## Thứ tự bắt buộc

Port và Eureka (và route Gateway) **đều lấy từ Config Server** – các service không khai báo trong code, chỉ cần `spring.config.import` trỏ tới config-server. Trong config-repo, user/product dùng **port 0** (port ngẫu nhiên); Eureka tự lưu port thực tế, Gateway gọi qua tên service. Vì vậy:

1. **Config Server** (port 8888) – **bắt buộc chạy đầu tiên** (user/product/gateway sẽ lấy config từ đây khi khởi động)  
2. **Discovery Server – Eureka** (port 8761) – bắt buộc chạy trước Gateway và các service  
3. User Service (port lấy từ config-repo)  
4. Product Service (port lấy từ config-repo)  
5. API Gateway (port + routes lấy từ config-repo)  

## Cách 1: Script PowerShell (khuyến nghị)

Trong thư mục `microservice` chạy:

```powershell
.\start-services.ps1
```

Script sẽ mở lần lượt 5 cửa sổ và chạy đúng thứ tự. Đợi mỗi service in dòng `Started ...Application` rồi mới coi như sẵn sàng.

## Cách 2: Chạy tay từng terminal

Mở **5 terminal**, chạy lần lượt và **đợi service trước chạy xong** rồi mới chạy service sau:

```powershell
# Terminal 1
cd config-server
mvn spring-boot:run
# Đợi thấy "Started ConfigServerApplication"

# Terminal 2 (sau khi Config Server đã chạy)
cd discovery-server
mvn spring-boot:run
# Đợi thấy "Started DiscoveryServerApplication" – quan trọng!

# Terminal 3
cd user-service
mvn spring-boot:run

# Terminal 4
cd product-service
mvn spring-boot:run

# Terminal 5 (chỉ chạy sau khi Eureka đã lên)
cd api-gateway
mvn spring-boot:run
```

## Kiểm tra

- Eureka: http://localhost:8761 (phải thấy API-GATEWAY, USER-SERVICE, PRODUCT-SERVICE)
- Qua Gateway: http://localhost:8080/users/1 và http://localhost:8080/products/1
