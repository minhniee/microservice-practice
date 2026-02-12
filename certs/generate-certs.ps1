# Generate CA and server/client certs for dev (run from certs/ directory)
# Requires JDK keytool in PATH
$PASS = "changeit"
$ErrorActionPreference = "Stop"
if (-not (Test-Path .)) { New-Item -ItemType Directory -Path . | Out-Null }

# 1. CA
Write-Host "Creating CA..."
keytool -genkeypair -alias ca -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore ca.p12 -storepass $PASS -keypass $PASS -dname "CN=MicroserviceCA" -ext bc:c

keytool -exportcert -alias ca -keystore ca.p12 -storepass $PASS -file ca.cer

# 2. Truststore (clients verify servers)
Write-Host "Creating truststore..."
keytool -importcert -alias ca -file ca.cer -keystore truststore.p12 -storetype PKCS12 -storepass $PASS -noprompt

function New-ServerCert {
    param($Name, $Alias = "$Name-server")
    Write-Host "Creating $Name server cert..."
    keytool -genkeypair -alias $Alias -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore "$Name-server.p12" -storepass $PASS -keypass $PASS -dname "CN=$Name"
    keytool -certreq -alias $Alias -keystore "$Name-server.p12" -storepass $PASS -file "$Name-server.csr"
    keytool -gencert -alias ca -infile "$Name-server.csr" -outfile "$Name-server.cer" -keystore ca.p12 -storepass $PASS
    keytool -importcert -alias ca -file ca.cer -keystore "$Name-server.p12" -storepass $PASS -noprompt
    keytool -importcert -alias $Alias -file "$Name-server.cer" -keystore "$Name-server.p12" -storepass $PASS -noprompt
    Remove-Item "$Name-server.csr", "$Name-server.cer" -ErrorAction SilentlyContinue
}

New-ServerCert "user"
New-ServerCert "product"
New-ServerCert "order"

# 3. Client cert (for order-service and api-gateway calling user-service with mTLS)
Write-Host "Creating client cert..."
keytool -genkeypair -alias client -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore client.p12 -storepass $PASS -keypass $PASS -dname "CN=gateway-client"
keytool -certreq -alias client -keystore client.p12 -storepass $PASS -file client.csr
keytool -gencert -alias ca -infile client.csr -outfile client.cer -keystore ca.p12 -storepass $PASS
keytool -importcert -alias ca -file ca.cer -keystore client.p12 -storepass $PASS -noprompt
keytool -importcert -alias client -file client.cer -keystore client.p12 -storepass $PASS -noprompt
Remove-Item client.csr, client.cer -ErrorAction SilentlyContinue

Write-Host "Done. Keystores: ca.p12, truststore.p12, user-server.p12, product-server.p12, order-server.p12, client.p12"
