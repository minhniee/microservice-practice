#!/usr/bin/env bash
# Generate CA and server/client certs for dev (run from certs/ directory)
set -e
PASS=changeit
mkdir -p . 2>/dev/null || true

echo "Creating CA..."
keytool -genkeypair -alias ca -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore ca.p12 -storepass "$PASS" -keypass "$PASS" -dname "CN=MicroserviceCA" -ext bc:c
keytool -exportcert -alias ca -keystore ca.p12 -storepass "$PASS" -file ca.cer

echo "Creating truststore..."
keytool -importcert -alias ca -file ca.cer -keystore truststore.p12 -storetype PKCS12 -storepass "$PASS" -noprompt

for name in user product order; do
  echo "Creating $name server cert..."
  keytool -genkeypair -alias "${name}-server" -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore "${name}-server.p12" -storepass "$PASS" -keypass "$PASS" -dname "CN=$name"
  keytool -certreq -alias "${name}-server" -keystore "${name}-server.p12" -storepass "$PASS" -file "${name}-server.csr"
  keytool -gencert -alias ca -infile "${name}-server.csr" -outfile "${name}-server.cer" -keystore ca.p12 -storepass "$PASS"
  keytool -importcert -alias ca -file ca.cer -keystore "${name}-server.p12" -storepass "$PASS" -noprompt
  keytool -importcert -alias "${name}-server" -file "${name}-server.cer" -keystore "${name}-server.p12" -storepass "$PASS" -noprompt
  rm -f "${name}-server.csr" "${name}-server.cer"
done

echo "Creating client cert..."
keytool -genkeypair -alias client -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore client.p12 -storepass "$PASS" -keypass "$PASS" -dname "CN=gateway-client"
keytool -certreq -alias client -keystore client.p12 -storepass "$PASS" -file client.csr
keytool -gencert -alias ca -infile client.csr -outfile client.cer -keystore ca.p12 -storepass "$PASS"
keytool -importcert -alias ca -file ca.cer -keystore client.p12 -storepass "$PASS" -noprompt
keytool -importcert -alias client -file client.cer -keystore client.p12 -storepass "$PASS" -noprompt
rm -f client.csr client.cer

echo "Done. Keystores: ca.p12, truststore.p12, user-server.p12, product-server.p12, order-server.p12, client.p12"
