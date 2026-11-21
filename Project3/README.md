# Project 3 - Securing an Online Payment System (Chapters 9-12)

Residency Day 2, Secure Software Development (MSCS-535).

- `pseudocode.txt` - the defense plan for an online payment system (parameterized queries, validation, output encoding, CSP).
- `vulnerable/` - payment demo where login concatenates user input into SQL (bypass: username `alice' -- `, any password) and payment notes render raw (stored XSS).
- `secure/` - same app with prepared statements, whitelist validation, HTML escaping at render, and a Content Security Policy.

`tests-evidence.txt` is the actual run log: the injection payload logs in on the vulnerable app and fails on the secured one; the stored script renders raw vs escaped.

## Run

```
cd vulnerable && npm install && node --experimental-sqlite server.js   # http://localhost:4201
cd secure     && npm install && node --experimental-sqlite server.js   # http://localhost:4202
```

Demo credentials: alice / winter2026. Local demonstration target only.
