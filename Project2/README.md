# Project 2 - JavaScript Code Injection and Dynamic Evaluation (Chapters 5-8)

Residency Day 2, Secure Software Development (MSCS-535).

Two small Express apps, identical in function:

- `vulnerable/` - guestbook with stored XSS, reflected XSS via `/search`, and a `/calc` endpoint that passes user input to `eval()`.
- `secure/` - same app with output encoding, a Content Security Policy (`script-src 'self'`, no `unsafe-inline`), input validation, and a whitelist parser replacing `eval()`.

`tests-evidence.txt` is the actual run log: each attack succeeds against the vulnerable app and fails against the secured one.

## Run

```
cd vulnerable && npm install && node server.js   # http://localhost:4101
cd secure     && npm install && node server.js   # http://localhost:4102
```

Both apps are local demonstration targets only.
