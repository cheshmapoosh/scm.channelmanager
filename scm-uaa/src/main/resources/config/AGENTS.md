# Agent Guide — Local SSL for SCM

## Scope

This guide is exclusively for configuring and troubleshooting local HTTPS between `scm-uaa` and JVM clients such as `scm-web`.

Do not change authentication flows, JWT claims, cookies, Camel routes, providers, business logic, databases, caches, or production TLS configuration unless the user explicitly expands the scope.

## Environment

- Java: 21
- Framework: Spring Boot 3.x
- UAA HTTPS endpoint: `https://localhost:8443`
- JWK endpoint: `https://localhost:8443/oauth2/jwks`
- UAA keystore directory: `scm-uaa/config`
- Web truststore directory: `scm-web/config`
- Keystore format: PKCS12

Never create SSL artifacts under `/tmp`. Keep local SSL files in the relevant module's `config` directory.

## Safety Rules

- Treat `.p12`, `.jks`, private keys, certificates, and passwords as sensitive.
- Never print private keys or commit them to Git.
- Ensure local keystores and truststores are ignored by Git.
- Do not modify the system-wide JVM `cacerts` for local testing.
- Do not disable hostname verification or certificate validation.
- Do not use insecure trust-all `TrustManager`, `HostnameVerifier`, or custom HTTP clients.
- Prefer a separate local Spring profile such as `local-ssl`.
- Before replacing a keystore or truststore, inspect the exact path, alias, type, and fingerprint.

## Required Certificate

The local UAA certificate must contain:

- Subject/Common Name: `CN=localhost`
- Subject Alternative Name: `DNS:localhost`
- Subject Alternative Name: `IP Address:127.0.0.1`
- Key algorithm: RSA
- Key size: at least 2048 bits

Do not rely only on the Common Name. Java 21 validates the Subject Alternative Name.

## Generate the Local UAA Keystore

Run from the repository root:

```bash
mkdir -p scm-uaa/config scm-web/config

keytool -genkeypair \
  -alias localhost \
  -keyalg RSA \
  -keysize 2048 \
  -validity 3650 \
  -storetype PKCS12 \
  -keystore scm-uaa/config/uaa-localhost.p12 \
  -storepass password \
  -keypass password \
  -dname 'CN=localhost, OU=Development, O=DaneshRefah, L=Tehran, ST=Tehran, C=IR' \
  -ext 'SAN=dns:localhost,ip:127.0.0.1'
```

If the target file already exists, do not silently overwrite it. Inspect it first or create a new explicitly named file.

## Configure Spring Boot UAA

Configure web-server SSL under `server.ssl`, not `spring.ssl`:

```yaml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: file:./config/uaa-localhost.p12
    key-store-type: PKCS12
    key-store-password: password
    key-password: password
    key-alias: localhost
```

When using a relative `file:./config/...` location, ensure the UAA working directory is `scm-uaa`. Otherwise, use a verified absolute path without Markdown or `mailto:` formatting.

After changing the keystore, fully stop and restart UAA. Confirm that no stale process is still listening:

```bash
ss -ltnp | grep ':8443'
```

## Verify the Certificate Served by UAA

Always verify the certificate actually served on port 8443, not only the certificate stored on disk:

```bash
openssl s_client \
  -connect localhost:8443 \
  -servername localhost </dev/null 2>/dev/null |
openssl x509 -noout -subject -issuer -fingerprint -sha256 -ext subjectAltName
```

Expected result:

```text
CN = localhost
DNS:localhost
IP Address:127.0.0.1
```

If the output still shows an older subject such as `CN=Java Hipster`, UAA is loading another keystore or another process is listening on port 8443. Do not update the client truststore until the served certificate is correct.

## Export the Active UAA Certificate

Export the certificate directly from the active HTTPS endpoint:

```bash
openssl s_client \
  -connect localhost:8443 \
  -servername localhost \
  -showcerts </dev/null 2>/dev/null |
openssl x509 -outform PEM \
  > scm-web/config/uaa-active.crt
```

Verify it:

```bash
openssl x509 \
  -in scm-web/config/uaa-active.crt \
  -noout \
  -subject \
  -issuer \
  -fingerprint \
  -sha256 \
  -ext subjectAltName
```

## Create the scm-web TrustStore

Import the active public certificate into a dedicated project-local truststore:

```bash
keytool -importcert \
  -alias uaa-local \
  -file scm-web/config/uaa-active.crt \
  -keystore scm-web/config/scm-web-truststore.p12 \
  -storetype PKCS12 \
  -storepass changeit \
  -noprompt
```

Inspect the imported certificate:

```bash
keytool -list -v \
  -alias uaa-local \
  -keystore scm-web/config/scm-web-truststore.p12 \
  -storetype PKCS12 \
  -storepass changeit
```

Compare the SHA-256 fingerprint in the truststore with the fingerprint served by UAA. They must match exactly.

## Configure the scm-web JVM

Use VM options similar to the following, with the verified absolute path from `realpath`:

```text
-Djavax.net.ssl.trustStore=/absolute/path/channelmanager/scm-web/config/scm-web-truststore.p12
-Djavax.net.ssl.trustStoreType=PKCS12
-Djavax.net.ssl.trustStorePassword=changeit
```

Obtain the exact path with:

```bash
realpath scm-web/config/scm-web-truststore.p12
```

Do not paste Markdown links, `mailto:` fragments, escaping characters, or blank values into VM options. Fully restart `scm-web` after changing the truststore or VM options.

## Verification

Test UAA independently:

```bash
curl -vk https://localhost:8443/oauth2/jwks
```

Use `-k` only for this direct curl diagnostic. JVM clients must validate the certificate through the configured truststore.

Confirm that `scm-web` starts with the expected JVM arguments and can retrieve the JWK set without an SSL exception.

## Error Diagnosis

| Error | Meaning | Correct action |
| --- | --- | --- |
| `wrong version number` | HTTPS was sent to a plain HTTP connector | Verify `server.port`, `server.ssl.enabled`, and the active process on the port |
| `problem accessing trust store` | Truststore path, type, password, or permissions are wrong | Validate with `keytool -list` using the same path/type/password |
| `No name matching localhost found` | Served certificate lacks `SAN=DNS:localhost` | Generate and activate a certificate with the required SAN values |
| `PKIX path building failed` | Active server certificate is not trusted | Export the active certificate, import it, and compare fingerprints |
| `Jwt expired` | TLS and certificate validation succeeded; the token expired | Stop SSL troubleshooting and obtain a fresh token |

## Completion Criteria

SSL work is complete only when all conditions are true:

1. UAA serves HTTPS on port 8443.
2. The served certificate has SAN entries for `localhost` and `127.0.0.1`.
3. The fingerprint served by UAA matches the certificate in the `scm-web` truststore.
4. `scm-web` starts with the correct truststore VM options.
5. Fetching `/oauth2/jwks` from `scm-web` no longer produces hostname, PKIX, or truststore errors.

Once these conditions pass, do not attribute later JWT expiry, Camel, provider, or HTTP 500 errors to SSL without new TLS-specific evidence.
