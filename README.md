# MSCS 535 - Secure Software Development

Coursework repository for MSCS 535 (University of the Cumberlands, Executive MSCS).

Each project lives in its own folder.

## Projects

- [Project1/](Project1/) - Secure Database Access over HTTPS: a Spring Boot REST API that is HTTPS-only, protects against SQL injection through parameterized JPA queries, hashes stored passwords with BCrypt, and enforces role-based access with CSRF protection. Includes a 9-test automated security test suite.
- [Project2/](Project2/) - JavaScript Code Injection and Dynamic Evaluation: a deliberately vulnerable guestbook (stored and reflected XSS, plus an eval() calculator) and its secured version with output encoding, a Content Security Policy, and a whitelist parser replacing eval(). Includes the actual exploit/mitigation run log.
- [Project3/](Project3/) - Securing an Online Payment System: pseudocode plus working vulnerable and secured implementations covering SQL injection (parameterized queries, whitelist validation) and cross-site scripting (output encoding, CSP). Includes the actual run log.
- [Project4/](Project4/) - Secure Online Communication with TLS: pseudocode plus a working demonstration where a client with certificate validation off accepts a simulated man-in-the-middle's rogue certificate, while the validating client rejects it and completes a TLS 1.3 session with the genuine server.
