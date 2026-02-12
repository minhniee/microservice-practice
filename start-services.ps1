# Chay cac microservice dung thu tu (bat buoc)
# Mo 5 cua so PowerShell va chay lan luot, HOAC chay script nay (mo 1 terminal cho moi service)

$root = $PSScriptRoot
if (-not $root) { $root = Get-Location }

Write-Host "=== THU TU CHAY (bat buoc) ===" -ForegroundColor Cyan
Write-Host "1. Config Server    - port 8888"
Write-Host "2. Discovery (Eureka) - port 8761  <-- Neu khong chay, Gateway/User/Product se bao Connection refused"
Write-Host "3. User Service     - port 8089"
Write-Host "4. Product Service - port 8082"
Write-Host "5. API Gateway     - port 8080"
Write-Host ""
Write-Host "Dang khoi dong Config Server (8888)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\config-server'; Write-Host 'Cho Config Server khoi dong xong (thay dong Started ConfigServerApplication), roi nhan Enter de tiep tuc' -ForegroundColor Green; mvn spring-boot:run"
Write-Host "Cho 15 giay de Config Server bat dau..."
Start-Sleep -Seconds 15

Write-Host "Dang khoi dong Discovery Server - Eureka (8761)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\discovery-server'; Write-Host 'Cho Eureka khoi dong xong (thay dong Started DiscoveryServerApplication), roi co the chay cac service khac.' -ForegroundColor Green; mvn spring-boot:run"
Write-Host "Cho 20 giay de Eureka bat dau (quan trong!)..."
Start-Sleep -Seconds 20

Write-Host "Dang khoi dong User Service (8089)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\user-service'; mvn spring-boot:run"
Start-Sleep -Seconds 3

Write-Host "Dang khoi dong Product Service (8082)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\product-service'; mvn spring-boot:run"
Start-Sleep -Seconds 3

Write-Host "Dang khoi dong API Gateway (8080)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\api-gateway'; mvn spring-boot:run"

Write-Host ""
Write-Host "Tat ca da duoc mo trong cac cua so moi." -ForegroundColor Green
Write-Host "Cho 30-60 giay de cac service dang ky Eureka, sau do thu:"
Write-Host "  Eureka:  http://localhost:8761"
Write-Host "  Gateway: http://localhost:8080/users/1  va  http://localhost:8080/products/1"
