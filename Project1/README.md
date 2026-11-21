# Project 1 - Secure Database Access over HTTPS

Residency Day 1, Project 1 (Chapters 1-4, Secure Software Design).

## Scenario

A phishing email pretending to be from the IT department asks employees for
login credentials, and a web application is vulnerable to SQL injection.
This project is the secured version of that application.

What this application controls:

- **SQL injection: removed.** All database access goes through Spring Data
  JPA parameterized queries; user input is never concatenated into SQL.
- **Eavesdropping: removed.** The API listens on HTTPS (TLS) only - there is
  no plain HTTP connector at all - so credentials and data are encrypted in
  transit.
- **Password storage: hashed.** The user store hashes passwords with BCrypt,
  so what is kept per account is the hash rather than the password itself.
  The demo defaults in `application.properties` are synthetic classroom
  placeholders meant to be overridden with environment variables in any real
  deployment.
- **Access control: enforced.** Every endpoint requires authentication, and
  write operations additionally require the ADMIN role.
- **CSRF: protected.** Mutating requests must echo the token from the
  `XSRF-TOKEN` cookie in the `X-XSRF-TOKEN` header.

What this application does NOT control: phishing itself. If an employee is
tricked into revealing valid credentials, TLS and hashing do not stop those
credentials from being replayed. That gap is closed by measures outside this
code, above all multi-factor authentication, plus user awareness training
and email filtering.

## Tech stack

- Java 21
- Spring Boot 4.1.3 (Spring Security 7, Spring Data JPA)
- H2 in-memory database (reachable only through the JPA repository; H2
  console disabled)

## Run

1. Generate the TLS keystore once (the keystore itself is intentionally not
   committed):

   ```sh
   keytool -genkeypair -alias securedb -keyalg RSA -keysize 2048 \
     -storetype PKCS12 -keystore src/main/resources/keystore.p12 \
     -validity 3650 -storepass <keystore-password> \
     -dname "CN=localhost, OU=CIS, O=University of the Cumberlands, L=Williamsburg, ST=KY, C=US"
   ```

2. Start the app:

   ```sh
   mvn spring-boot:run
   ```

   It serves `https://localhost:8443` with a self-signed certificate, so use
   `curl -k`.

3. Demo accounts (synthetic classroom placeholders from
   `application.properties`; override with the `ADMIN_USERNAME`,
   `ADMIN_PASSWORD`, `USER_USERNAME`, `USER_PASSWORD` environment variables):

   - `admin` / `Admin@123` - role ADMIN
   - `user` / `User@123` - role USER

## API

| Method | Endpoint                              | Role required        |
| ------ | ------------------------------------- | -------------------- |
| GET    | /api/csrf                             | any authenticated    |
| GET    | /api/employees                        | USER or ADMIN        |
| GET    | /api/employees/{id}                   | USER or ADMIN        |
| GET    | /api/employees/department/{dept}      | USER or ADMIN        |
| POST   | /api/employees                        | ADMIN + CSRF token   |
| DELETE | /api/employees/{id}                   | ADMIN + CSRF token   |

Read example:

```sh
curl -k -u user:User@123 https://localhost:8443/api/employees
```

Write example (fetch the CSRF token, then echo it back):

```sh
curl -k -u admin:Admin@123 -c cookies.txt https://localhost:8443/api/csrf
TOKEN=$(curl -k -u admin:Admin@123 -b cookies.txt -c cookies.txt https://localhost:8443/api/csrf | jq -r .token)
curl -k -u admin:Admin@123 -b cookies.txt \
  -H "X-XSRF-TOKEN: $TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"Eve Adams","email":"eve@example.com","department":"HR"}' \
  https://localhost:8443/api/employees
```

SQL injection probe (returns an empty list - the payload is bound as a
literal parameter value, never executed as SQL):

```sh
curl -k -u user:User@123 "https://localhost:8443/api/employees/department/'%20OR%20'1'='1"
```

## Automated tests

```sh
mvn test
```

Nine MockMvc tests in `src/test/java/com/example/securedb/EmployeeApiTests.java`
exercise the real security filter chain: unauthenticated requests are
rejected (401), USER can read but not write (403 on mutations), ADMIN
mutations without a CSRF token are rejected, ADMIN with a token can create
and delete, a SQL injection payload matches nothing, and the final state
contains only the seeded records.

Latest run: **Tests run: 9, Failures: 0, Errors: 0, Skipped: 0.**

Two honest harness notes (also documented in the test class comment):

- MockMvc does not open a socket, so the TLS layer itself is not under test
  here; HTTPS-only behavior follows from there being no plain HTTP
  connector at all.
- With a valid login but no CSRF token, MockMvc answers 403 while curl over
  real HTTPS answers 401. Both are the same rejection - the mutation does
  not happen; only the status code differs between the two environments.

## Project layout

```
Project1/
  pom.xml
  src/main/java/com/example/securedb/
    SecureDbApplication.java          application entry point
    model/Employee.java               JPA entity
    repository/EmployeeRepository.java parameterized queries
    controller/EmployeeController.java  REST endpoints
    controller/CsrfController.java      CSRF token endpoint
    config/SecurityConfig.java          HTTPS/CSRF/roles/BCrypt setup
  src/main/resources/
    application.properties              TLS + demo accounts + H2 config
    data.sql                            two seeded employees
  src/test/java/com/example/securedb/
    EmployeeApiTests.java               9 security tests
```
