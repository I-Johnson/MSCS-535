package com.example.securedb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Residency Day 1 - Project 1 (Chapters 1-4, Secure Software Design)
 *
 * Scenario: a phishing email pretending to be from the IT department asks
 * employees for login credentials, and a web application is vulnerable to
 * SQL injection.
 *
 * What this application controls, honestly stated:
 *  - SQL injection: removed. All database access goes through Spring Data
 *    JPA parameterized queries; user input is never concatenated into SQL.
 *  - Eavesdropping: removed. The API listens on HTTPS (TLS) only, so
 *    credentials and data are encrypted in transit.
 *  - Password storage: the user store hashes passwords with BCrypt, so
 *    what is kept per account is the hash rather than the password itself.
 *    Note the distinction: the demo defaults do appear in
 *    application.properties, because they are synthetic classroom
 *    placeholders meant to be overridden with environment variables in any
 *    real deployment.
 *  - Access control: enforced. Every endpoint requires authentication, and
 *    write operations additionally require the ADMIN role.
 *
 * What this application does NOT control: phishing itself. If an employee
 * is tricked into revealing valid credentials, TLS and hashing do not stop
 * those credentials from being replayed. That gap is closed by measures
 * outside this code, above all multi-factor authentication, plus user
 * awareness training and email filtering - which is why organizations
 * should not rely on passwords alone.
 */
@SpringBootApplication
public class SecureDbApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecureDbApplication.class, args);
    }
}
