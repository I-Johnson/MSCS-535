# Project 4 - Secure Online Communication with TLS (Chapters 13-16)

Residency Day 3, Secure Software Development (MSCS-535).

- `pseudocode.txt` - the design: TLS channel with certificate chain validation, hostname verification, AEAD integrity and forward secrecy, and why each defeats a man in the middle.
- `src/server.js` - genuine TLS server (CA-signed localhost certificate), port 4301.
- `src/rogue-server.js` - simulated MITM presenting a self-made localhost certificate, port 4302.
- `src/insecure-client.js` - the mistake: `rejectUnauthorized: false`, accepts any certificate.
- `src/secure-client.js` - validates the chain against the trusted CA and verifies the hostname.
- `gen-certs.sh` - regenerates all demo certificates (openssl required).

`tests-evidence.txt` is the actual run log: the insecure client accepts the rogue certificate and receives the attacker's message; the secure client rejects it (`unable to verify the first certificate`) and completes a TLS 1.3 handshake (TLS_AES_256_GCM_SHA384) with the genuine server.

## Run

```
sh gen-certs.sh
node src/server.js &        # genuine server on 4301
node src/rogue-server.js &  # simulated MITM on 4302
node src/insecure-client.js 4302   # accepts the attacker's cert (bad)
node src/secure-client.js 4302     # rejected before any data flows (good)
node src/secure-client.js 4301     # validated TLS 1.3 session (good)
```

Local demonstration only.
